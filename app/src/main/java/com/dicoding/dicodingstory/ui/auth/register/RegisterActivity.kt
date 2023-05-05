package com.dicoding.dicodingstory.ui.auth.register

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
import com.dicoding.dicodingstory.databinding.ActivityRegisterBinding
import com.dicoding.dicodingstory.ui.AuthViewModelFactory
import com.dicoding.dicodingstory.ui.auth.login.LoginActivity
import kotlinx.coroutines.launch
import org.w3c.dom.Text

class RegisterActivity : AppCompatActivity(), View.OnClickListener {

    private var _binding: ActivityRegisterBinding? = null
    private val binding get() = _binding!!
    private var _viewModel: RegisterViewModel? = null
    private val viewModel get() = _viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hideSystemUI()
        showPb(false)
        disableForm(state = false, clear = true)
        playAnim()

        val etName = binding.etName
        val etEmail = binding.etName
        val etPass = binding.etPassword
        val btnRegister = binding.btnRegister
        var nameOk = false
        var emailOk = false
        var passOk = false

        etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                nameOk = etName.error == null

                btnRegister.isEnabled = nameOk && emailOk && passOk
            }

        })

        etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                emailOk = etEmail.error == null

                btnRegister.isEnabled = emailOk && nameOk && passOk
            }

        })

        etPass.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                passOk = etPass.error == null

                btnRegister.isEnabled = passOk && emailOk && nameOk
            }

        })

        val factory: AuthViewModelFactory = AuthViewModelFactory.getInstance(applicationContext)
        _viewModel = ViewModelProvider(this, factory)[RegisterViewModel::class.java]

        binding.tvRegistToLogin.setOnClickListener(this)

        binding.btnRegister.setOnClickListener(this)

    }

    private fun playAnim() {
        val img = ObjectAnimator.ofFloat(binding.imgRegister, View.ALPHA, 1f).setDuration(500)
        val signUpText = ObjectAnimator.ofFloat(binding.tvRegister, View.ALPHA, 1f).setDuration(500)
        val etName = ObjectAnimator.ofFloat(binding.etName, View.ALPHA, 1f).setDuration(500)
        val etEmail = ObjectAnimator.ofFloat(binding.etEmail, View.ALPHA, 1f).setDuration(500)
        val etPassword = ObjectAnimator.ofFloat(binding.etPassword, View.ALPHA, 1f).setDuration(500)
        val btnRegister =
            ObjectAnimator.ofFloat(binding.btnRegister, View.ALPHA, 1f).setDuration(500)
        val haveAccount =
            ObjectAnimator.ofFloat(binding.tvDontHaveAccount, View.ALPHA, 1f).setDuration(500)
        val loginText =
            ObjectAnimator.ofFloat(binding.tvRegistToLogin, View.ALPHA, 1f).setDuration(500)

        val together = AnimatorSet().apply {
            playTogether(etName, etEmail, etPassword)
        }

        AnimatorSet().apply {
            playSequentially(img, signUpText, together, btnRegister, haveAccount, loginText)
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
        binding.pbRegister.visibility = if (state) View.VISIBLE else View.GONE
    }

    private fun disableForm(state: Boolean, clear: Boolean) {
        binding.apply {
            etEmail.isEnabled = !state
            etName.isEnabled = !state
            etPassword.isEnabled = !state
            btnRegister.isEnabled = !state

            if (clear) {
                etEmail.text?.clear()
                etName.text?.clear()
                etPassword.text?.clear()
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_regist_to_login -> {
                val loginIntent = Intent(this@RegisterActivity, LoginActivity::class.java)
                loginIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                finish()
                startActivity(loginIntent)
            }
            R.id.btn_register -> {
                val name = binding.etName.text.toString()
                val email = binding.etEmail.text.toString()
                val password = binding.etPassword.text.toString()

                lifecycleScope.launch {
                    viewModel?.register(name, email, password)
                        ?.observe(this@RegisterActivity) { result ->
                            when (result) {
                                is Result.Loading -> {
                                    showPb(true)
                                    disableForm(state = true, clear = false)
                                }
                                is Result.Success -> {
                                    showPb(false)
                                    disableForm(state = false, clear = true)
                                    showMessage(result.data.message.toString())

                                    val loginIntent =
                                        Intent(this@RegisterActivity, LoginActivity::class.java)
                                    finish()
                                    startActivity(loginIntent)
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