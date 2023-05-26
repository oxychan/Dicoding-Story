package com.dicoding.dicodingstory.ui.maps

import android.content.res.Resources
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dicoding.dicodingstory.R
import com.dicoding.dicodingstory.data.Result
import com.dicoding.dicodingstory.data.models.UserModel
import com.dicoding.dicodingstory.data.response.Story
import com.dicoding.dicodingstory.databinding.ActivityMapsStoryBinding
import com.dicoding.dicodingstory.ui.StoryViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapsStoryActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsStoryBinding
    private lateinit var viewModel: MapsStoryViewModel
    private val stories: MutableList<Story> = mutableListOf()
    private val boundsBuilder = LatLngBounds.Builder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory: StoryViewModelFactory = StoryViewModelFactory.getInstance(applicationContext)
        viewModel = ViewModelProvider(this, factory)[MapsStoryViewModel::class.java]

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        var userModel: UserModel? = null
        viewModel.getAuthenticatedUser().observe(this) { user ->
            if (user.isAuthenticated) {
                userModel = UserModel(
                    name = user.name,
                    token = user.token,
                    isAuthenticated = true
                )
            }
        }

        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.getStories(userModel?.token.toString(), 1, 10, 1)
                .observe(this@MapsStoryActivity) { result ->
                    when (result) {
                        is Result.Loading -> {

                        }
                        is Result.Success -> {
                            stories.clear()
                            result.data.listStory?.forEach { story ->
                                if (story != null) {
                                    stories.add(story)
                                }
                                mapFragment.getMapAsync(this@MapsStoryActivity)
                            }
                        }
                        is Result.Error -> {
                            Toast.makeText(this@MapsStoryActivity, result.error, Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            Toast.makeText(this@MapsStoryActivity, "Something wrong", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        setMapStyle()
        showUsersMarker()
    }

    private fun showUsersMarker() {
        stories.forEach { story ->
            val latLng = LatLng(story.lat as Double, story.lon as Double)
            mMap.addMarker(MarkerOptions().position(latLng).title(story.name))
            boundsBuilder.include(latLng)
        }

        val bounds: LatLngBounds = boundsBuilder.build()
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds,
                resources.displayMetrics.widthPixels,
                resources.displayMetrics.heightPixels,
                300
            )
        )
    }

    private fun setMapStyle() {
        try {
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
        } catch (e: Resources.NotFoundException) {
            Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}