package com.dicoding.dicodingstory.ui.auth.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.dicoding.dicodingstory.data.Result
import com.dicoding.dicodingstory.data.repository.AuthRepository
import com.dicoding.dicodingstory.data.response.RegisterResponse

class RegisterViewModel(private val authRepository: AuthRepository) : ViewModel() {
    suspend fun register(
        name: String,
        email: String,
        password: String
    ): LiveData<Result<RegisterResponse>> {
        return authRepository.register(name, email, password)
    }

}