package com.dicoding.dicodingstory.ui.auth.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dicoding.dicodingstory.R
import com.dicoding.dicodingstory.data.Result
import com.dicoding.dicodingstory.databinding.ActivityLoginBinding
import com.dicoding.dicodingstory.ui.AuthViewModelFactory
import com.dicoding.dicodingstory.ui.auth.register.RegisterActivity
import com.dicoding.dicodingstory.ui.dashboard.MainActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!

    private var _viewModel: LoginViewModel? = null
    private val viewModel get() = _viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideSystemUI()

        val btnLogin = binding.btnLogin
        val etEmail = binding.etEmail
        var emailOk = false
        val etPass = binding.etPassword
        var passOk = false

        etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                emailOk = etEmail.error == null

                binding.btnLogin.isEnabled = emailOk && passOk
            }
        })

        etPass.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                passOk = etPass.error == null

                btnLogin.isEnabled = passOk && emailOk
            }
        })
        showPb(false)
        disableForm(state = false, clear = false)
        playAnim()

        val factory: AuthViewModelFactory = AuthViewModelFactory.getInstance(applicationContext)
        _viewModel = ViewModelProvider(this, factory)[LoginViewModel::class.java]

        viewModel?.getAuthenticatedUser()?.observe(this) { authenticatedUser ->
            if (authenticatedUser.isAuthenticated) {
                disableForm(state = true, clear = true)
                showPb(true)
                val dashboardIntent = Intent(this, MainActivity::class.java)
                dashboardIntent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                finish()
                startActivity(dashboardIntent)
            }
        }

        binding.tvLoginToRegister.setOnClickListener(this)

        binding.btnLogin.setOnClickListener(this)


    }


    private fun playAnim() {
        val img = ObjectAnimator.ofFloat(binding.imgLogin, View.ALPHA, 1f).setDuration(500)
        val signInText = ObjectAnimator.ofFloat(binding.tvLogin, View.ALPHA, 1f).setDuration(500)
        val etEmail = ObjectAnimator.ofFloat(binding.etEmail, View.ALPHA, 1f).setDuration(500)
        val etPassword = ObjectAnimator.ofFloat(binding.etPassword, View.ALPHA, 1f).setDuration(500)
        val btnLogin = ObjectAnimator.ofFloat(binding.btnLogin, View.ALPHA, 1f).setDuration(500)
        val dontHaveAccount =
            ObjectAnimator.ofFloat(binding.tvDontHaveAccount, View.ALPHA, 1f).setDuration(500)
        val registerText =
            ObjectAnimator.ofFloat(binding.tvLoginToRegister, View.ALPHA, 1f).setDuration(500)

        val together = AnimatorSet().apply {
            playTogether(etEmail, etPassword, btnLogin)
        }

        AnimatorSet().apply {
            playSequentially(img, signInText, together, dontHaveAccount, registerText)
            start()
        }
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
        binding.pbLogin.visibility = if (state) View.VISIBLE else View.GONE
    }

    private fun disableForm(state: Boolean, clear: Boolean) {
        binding.apply {
            etEmail.isEnabled = !state
            etPassword.isEnabled = !state
            btnLogin.isEnabled = !state

            if (clear) {
                etEmail.text?.clear()
                etPassword.text?.clear()
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_login_to_register -> {
                val registerIntent = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(registerIntent)
            }
            R.id.btn_login -> {
                val email = binding.etEmail.text.toString()
                val password = binding.etPassword.text.toString()
                lifecycleScope.launch {
                    viewModel?.login(email, password)?.observe(this@LoginActivity) { result ->
                        when (result) {
                            is Result.Loading -> {
                                showPb(true)
                                disableForm(state = true, clear = false)
                            }
                            is Result.Success -> {
                                showPb(false)
                                disableForm(state = false, clear = true)
                                showMessage(result.data.message.toString())
                            }
                            is Result.Error -> {
                                showPb(false)
                                disableForm(state = false, clear = false)
                                showMessage(result.error)
                            }
                            else -> {
                                showMessage("Something wrong")
                            }
                        }
                    }
                }
            }
        }
    }
}