package com.dicoding.dicodingstory.ui.dashboard

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.dicodingstory.R
import com.dicoding.dicodingstory.data.Result
import com.dicoding.dicodingstory.data.models.StoryModel
import com.dicoding.dicodingstory.data.models.UserModel
import com.dicoding.dicodingstory.data.response.Story
import com.dicoding.dicodingstory.databinding.ActivityMainBinding
import com.dicoding.dicodingstory.ui.StoryAdapter
import com.dicoding.dicodingstory.ui.StoryViewModelFactory
import com.dicoding.dicodingstory.ui.auth.login.LoginActivity
import com.dicoding.dicodingstory.ui.story.AddStoryActivity
import kotlinx.coroutines.Dispatchers
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

        binding.ivBtnSetting.setOnClickListener(this)

        val layoutManager = LinearLayoutManager(applicationContext)
        binding.rvStories.apply {
            this.layoutManager = layoutManager
            this.adapter = StoryAdapter(ArrayList())
        }

        val factory: StoryViewModelFactory = StoryViewModelFactory.getInstance(applicationContext)
        _viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        var userModel: UserModel? = null
        viewModel?.getAuthenticatedUser()?.observe(this) { user ->
            val name = user.name.substringBefore(" ")
            binding.tvName.text = resources.getString(R.string.hello_name, name)
            if (user.isAuthenticated) {
                userModel = UserModel(
                    name = user.name,
                    token = user.token,
                    isAuthenticated = true
                )
            }
            greetings()
            playAnim()
        }

        lifecycleScope.launch(Dispatchers.Main) {
            viewModel?.getStories(userModel?.token.toString(), 1, 10, 0)
                ?.observe(this@MainActivity) { result ->
                    when (result) {
                        is Result.Loading -> {
                            showPb(true)
                        }
                        is Result.Success -> {
                            showPb(false)
                            setAdapterData(result.data.listStory)
                        }
                        is Result.Error -> {
                            showPb(false)
                            showMessage(result.error)
                        }
                        else -> {
                            showMessage("Something wrong")
                        }
                    }
                }
        }

        binding.ivBtnLogout.setOnClickListener(this)

        binding.fabAddStory.setOnClickListener(this)
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

            @Suppress("DEPRECATION")
            val hour = timeNow.hours

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

    private fun setAdapterData(stories: List<Story?>?) {
        val tempStories = ArrayList<StoryModel>()
        if (stories != null) {
            for (story in stories) {
                tempStories.add(
                    StoryModel(
                        name = story?.name ?: "",
                        photoUrl = story?.photoUrl ?: "",
                        description = story?.description ?: "",
                        createdAt = story?.createdAt
                    )
                )
            }
        }

        val adapter = StoryAdapter(tempStories)
        adapter.setOnItemClickCallback(object : StoryAdapter.OnItemClickCallback {
            override fun onItemClicked(data: StoryModel) {
                val detailIntent = Intent(this@MainActivity, DetailStoryActivity::class.java)
                detailIntent.putExtra(EXTRA_STORY, data)
                detailIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(detailIntent)
            }

        })

        binding.rvStories.adapter = adapter
    }

    private fun hideSystemUI() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showPb(state: Boolean) {
        binding.pbDashboard.visibility = if (state) View.VISIBLE else View.GONE
    }


    override fun onResume() {
        super.onResume()

        var userModel: UserModel? = null
        viewModel?.getAuthenticatedUser()?.observe(this) { user ->
            val name = user.name.substringBefore(" ")
            binding.tvName.text = resources.getString(R.string.hello_name, name)
            if (user.isAuthenticated) {
                userModel = UserModel(
                    name = user.name,
                    token = user.token,
                    isAuthenticated = true
                )
            }
        }

        lifecycleScope.launch(Dispatchers.Main) {
            viewModel?.getStories(userModel?.token.toString(), 1, 10, 0)
                ?.observe(this@MainActivity) { result ->
                    when (result) {
                        is Result.Loading -> {
                            showPb(true)
                        }
                        is Result.Success -> {
                            showPb(false)
                            setAdapterData(result.data.listStory)
                        }
                        is Result.Error -> {
                            showPb(false)
                            showMessage(result.error)
                        }
                        else -> {
                            showMessage("Something wrong")
                        }
                    }
                }
        }
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
        }
    }

    companion object {
        const val EXTRA_STORY = "extra_story"
    }

}