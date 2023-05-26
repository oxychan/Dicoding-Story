package com.dicoding.dicodingstory.ui.story

import android.Manifest
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dicoding.dicodingstory.data.Result
import com.dicoding.dicodingstory.data.response.StoryResponse
import com.dicoding.dicodingstory.databinding.ActivityAddStoryBinding
import com.dicoding.dicodingstory.ui.StoryViewModelFactory
import com.dicoding.dicodingstory.ui.dashboard.MainActivity
import com.dicoding.dicodingstory.utils.Utils
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class AddStoryActivity : AppCompatActivity() {

    private var _binding: ActivityAddStoryBinding? = null
    private val binding get() = _binding!!

    private var _viewModel: StoryViewModel? = null
    private val viewModel get() = _viewModel

    private var getFile: File? = null

    private lateinit var locationRequest: LocationRequest
    private lateinit var fuseLocationClient: FusedLocationProviderClient

    private var _location: Location? = null
    private val location: Location? get() = _location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showPb(false)

        binding.etDesc.isEnabled = getFile != null
        binding.cbLocation.isEnabled = getFile != null

        val factory: StoryViewModelFactory = StoryViewModelFactory.getInstance(applicationContext)
        _viewModel = ViewModelProvider(this, factory)[StoryViewModel::class.java]

        if (!allPermissionGranted() && !checkLocationPermission()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        var isLocationChecked: Boolean = false
        binding.cbLocation.setOnCheckedChangeListener { _, isChecked ->
            isLocationChecked = isChecked
        }


        binding.btnCameraX.setOnClickListener { startCameraX() }
        binding.btnGallery.setOnClickListener { startGallery() }

        binding.btnUpload.setOnClickListener {
            viewModel?.getAuthenticatedUser()?.observe(this@AddStoryActivity) { authenticatedUser ->
                lifecycleScope.launch(Dispatchers.Main) {
                    val desc = binding.etDesc.text.toString()
                    viewModel?.apply {
                        if (isLocationChecked) {
                            postStory(
                                authenticatedUser.token,
                                desc,
                                location?.latitude!!.toFloat(),
                                location?.longitude!!.toFloat(),
                                getFile
                            ).observe(this@AddStoryActivity) { result ->
                                responseHandler(result)
                            }
                        } else {
                            postStory(
                                authenticatedUser.token,
                                desc,
                                getFile
                            ).observe(this@AddStoryActivity) { result ->
                                responseHandler(result)
                            }
                        }
                    }
                }
            }
        }

        fuseLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()
    }

    private fun responseHandler(result: Result<StoryResponse>) {
        when (result) {
            is Result.Loading -> {
                disableForm(state = true, clear = false)
                showPb(true)
            }
            is Result.Success -> {
                showPb(false)
                disableForm(state = true, clear = true)
                val intent =
                    Intent(this@AddStoryActivity, MainActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            is Result.Error -> {
                disableForm(state = false, clear = true)
                showMessage(result.error)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        createLocationRequest()
    }

    private fun startCameraX() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERA_X_RESULT) {
            val myFile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.data?.getSerializableExtra("picture", File::class.java)
            } else {
                @Suppress("DEPRECATION")
                it.data?.getSerializableExtra("picture")
            } as? File

            val isBackCamera = it.data?.getBooleanExtra("isBackCamera", true) as Boolean

            myFile?.let { file ->
//                Utils.rotateFile(file, isBackCamera)
                getFile = file
                binding.ivPreviewCam.setImageBitmap(BitmapFactory.decodeFile(file.path))
                binding.etDesc.isEnabled = true
                binding.cbLocation.isEnabled = true
            }
        }
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg = result.data?.data as Uri
            selectedImg.let { uri ->
                val myFile = Utils.uriToFile(uri, this@AddStoryActivity)
                getFile = myFile
                binding.ivPreviewCam.setImageURI(uri)
                binding.etDesc.isEnabled = true
                binding.cbLocation.isEnabled = true
            }
        }
    }

    private fun allPermissionGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionGranted()) {
                Toast.makeText(
                    this,
                    "Tidak mendapatkan izin",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private val resolutionLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            when (result.resultCode) {
                RESULT_CANCELED -> {
                    Toast.makeText(
                        this@AddStoryActivity,
                        "you must turn your GPS on!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    private fun checkLocationPermission() = LOCATION_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext,
            it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false ->
                    getCurrentLocation()
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false ->
                    getCurrentLocation()
            }
        }

    private fun getCurrentLocation() {
        if (checkLocationPermission()) {
            fuseLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    this._location = location
                    Log.d("YourCurrentLoc", "dieksekusi getCurrentLocation()")
                } else {
                    Toast.makeText(
                        this@AddStoryActivity,
                        "Location not found",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            requestPermissionLauncher.launch(
                LOCATION_PERMISSIONS
            )
        }
    }

    private fun createLocationRequest() {
        @Suppress("DEPRECATION")
        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
        client.checkLocationSettings(builder.build())
            .addOnSuccessListener {
                getCurrentLocation()
            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    try {
                        resolutionLauncher.launch(
                            IntentSenderRequest.Builder(exception.resolution).build()
                        )
                    } catch (sendEx: IntentSender.SendIntentException) {
                        Toast.makeText(
                            this@AddStoryActivity,
                            sendEx.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showPb(state: Boolean) {
        binding.pbAddStory.visibility = if (state) View.VISIBLE else View.GONE
    }

    private fun disableForm(state: Boolean, clear: Boolean) {
        binding.apply {
            btnUpload.isEnabled = !state
            btnGallery.isEnabled = !state
            btnCameraX.isEnabled = !state
            etDesc.isEnabled = !state
            cbLocation.isEnabled = !state

            if (clear) {
                etDesc.text.clear()
            }
        }
    }

    companion object {
        const val CAMERA_X_RESULT = 200

        private val LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            *LOCATION_PERMISSIONS
        )
        private const val REQUEST_CODE_PERMISSIONS = 10

    }
}