package com.dicoding.dicodingstory.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.dicodingstory.data.repository.AuthRepository
import com.dicoding.dicodingstory.di.Injection
import com.dicoding.dicodingstory.ui.auth.login.LoginViewModel
import com.dicoding.dicodingstory.ui.auth.register.RegisterViewModel

class AuthViewModelFactory private constructor(private val authRepository: AuthRepository) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            return RegisterViewModel(authRepository) as T
        } else if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(authRepository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }

    companion object {
        @Volatile
        private var instance: AuthViewModelFactory? = null
        fun getInstance(context: Context): AuthViewModelFactory =
            instance ?: synchronized(this) {
                instance ?: AuthViewModelFactory(Injection.authProviderRepository(context))
            }.also { instance = it }
    }
}