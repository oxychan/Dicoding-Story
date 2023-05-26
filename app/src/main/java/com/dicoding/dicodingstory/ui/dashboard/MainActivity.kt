package com.dicoding.dicodingstory.ui.dashboard

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.dicodingstory.R
import com.dicoding.dicodingstory.data.models.StoryModel
import com.dicoding.dicodingstory.databinding.ActivityMainBinding
import com.dicoding.dicodingstory.ui.LoadingStateAdapter
import com.dicoding.dicodingstory.ui.StoryAdapterWithPaging
import com.dicoding.dicodingstory.ui.StoryViewModelFactory
import com.dicoding.dicodingstory.ui.auth.login.LoginActivity
import com.dicoding.dicodingstory.ui.maps.MapsStoryActivity
import com.dicoding.dicodingstory.ui.story.AddStoryActivity
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private var _viewModel: MainViewModel? = null
    private val viewModel get() = _viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hideSystemUI()

        val layoutManager = LinearLayoutManager(applicationContext)
        binding.rvStories.apply {
            this.layoutManager = layoutManager
            this.adapter = StoryAdapterWithPaging()
        }

        val factory: StoryViewModelFactory = StoryViewModelFactory.getInstance(applicationContext)
        _viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        viewModel?.getAuthenticatedUser()?.observe(this) { user ->
            val name = user.name.substringBefore(" ")
            binding.tvName.text = resources.getString(R.string.hello_name, name)

            greetings()
            playAnim()
        }

        binding.ivBtnLogout.setOnClickListener(this)
        binding.ivBtnSetting.setOnClickListener(this)
        binding.fabShowMaps.setOnClickListener(this)
        binding.fabAddStory.setOnClickListener(this)

        setAdapterDataWithPaging()
    }

    private fun greetings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val now = LocalTime.now()

            binding.tvGreetings.text = when {
                now.isBefore(LocalTime.NOON) -> resources.getString(R.string.good_morning)
                now.isBefore(LocalTime.of(18, 0)) -> resources.getString(R.string.good_afternoon)
                else -> resources.getString(R.string.good_night)
            }
        } else {
            val timeNow = Calendar.getInstance().time

            @Suppress("DEPRECATION") val hour = timeNow.hours
            binding.tvGreetings.text = when {
                hour < 12 -> resources.getString(R.string.good_morning)
                hour < 18 -> resources.getString(R.string.good_afternoon)
                else -> resources.getString(R.string.good_night)
            }
        }
    }


    private fun playAnim() {
        val name = ObjectAnimator.ofFloat(binding.tvName, View.ALPHA, 1f).setDuration(800)
        val greets = ObjectAnimator.ofFloat(binding.tvGreetings, View.ALPHA, 1f).setDuration(800)
        val logout = ObjectAnimator.ofFloat(binding.ivBtnLogout, View.ALPHA, 1f).setDuration(700)

        val together = AnimatorSet().apply {
            playTogether(name, greets)
        }

        AnimatorSet().apply {
            playSequentially(together, logout)
            start()
        }
    }

    private fun setAdapterDataWithPaging() {
        val adapter = StoryAdapterWithPaging()
        binding.rvStories.adapter = adapter.withLoadStateFooter(
            footer = LoadingStateAdapter()
        )

        adapter.setOnItemClickCallback(object : StoryAdapterWithPaging.OnItemClickCallback {
            override fun onItemClicked(data: StoryModel) {
                val detailIntent = Intent(this@MainActivity, DetailStoryActivity::class.java)
                detailIntent.putExtra(EXTRA_STORY, data)
                detailIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(detailIntent)
            }

        })

        viewModel?.getStoriesWithPaging()?.observe(this) {
            Log.d("PaggingNih", "set adapter data")
            adapter.submitData(lifecycle, it)
        }
    }

    private fun hideSystemUI() {
        @Suppress("DEPRECATION") if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    override fun onResume() {
        super.onResume()
        setAdapterDataWithPaging()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_btn_logout -> {
                lifecycleScope.launch {
                    if (viewModel?.logout() == true) {
                        val loginIntent = Intent(this@MainActivity, LoginActivity::class.java)
                        loginIntent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        finish()
                        startActivity(loginIntent)
                    }
                }
            }
            R.id.fab_add_story -> {
                val addStoryIntent = Intent(this, AddStoryActivity::class.java)
                startActivity(addStoryIntent)
            }
            R.id.iv_btn_setting -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
            }
            R.id.fab_show_maps -> {
                val intent = Intent(this, MapsStoryActivity::class.java)
                startActivity(intent)
            }
        }
    }

    companion object {
        const val EXTRA_STORY = "extra_story"
    }

}