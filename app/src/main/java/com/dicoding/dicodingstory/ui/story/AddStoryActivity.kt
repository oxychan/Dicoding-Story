package com.dicoding.dicodingstory.ui.story

import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dicoding.dicodingstory.data.Result
import com.dicoding.dicodingstory.databinding.ActivityAddStoryBinding
import com.dicoding.dicodingstory.ui.StoryViewModelFactory
import com.dicoding.dicodingstory.ui.dashboard.MainActivity
import com.dicoding.dicodingstory.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class AddStoryActivity : AppCompatActivity() {

    private var _binding: ActivityAddStoryBinding? = null
    private val binding get() = _binding!!

    private var _viewModel: StoryViewModel? = null
    private val viewModel get() = _viewModel

    private var getFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showPb(false)

        binding.etDesc.isEnabled = getFile != null

        val factory: StoryViewModelFactory = StoryViewModelFactory.getInstance(applicationContext)
        _viewModel = ViewModelProvider(this, factory)[StoryViewModel::class.java]

        if (!allPermissionGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        binding.btnCameraX.setOnClickListener { startCameraX() }
        binding.btnGallery.setOnClickListener { startGallery() }

        binding.btnUpload.setOnClickListener {
            viewModel?.getAuthenticatedUser()?.observe(this@AddStoryActivity) { authenticatedUser ->
                lifecycleScope.launch(Dispatchers.Main) {
                    val desc = binding.etDesc.text.toString()
                    viewModel?.postStory(authenticatedUser.token, desc, getFile)
                        ?.observe(this@AddStoryActivity) { result ->
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
                }
            }
        }
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
                Utils.rotateFile(file, isBackCamera)
                getFile = file
                binding.ivPreviewCam.setImageBitmap(BitmapFactory.decodeFile(file.path))
                binding.etDesc.isEnabled = true
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

            if (clear) {
                etDesc.text.clear()
            }
        }
    }

    companion object {
        const val CAMERA_X_RESULT = 200

        private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}