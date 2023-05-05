package com.dicoding.dicodingstory.ui.auth.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.dicoding.dicodingstory.data.Result
import com.dicoding.dicodingstory.data.models.UserModel
import com.dicoding.dicodingstory.data.repository.AuthRepository
import com.dicoding.dicodingstory.data.response.LoginResponse

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {
    fun login(
        email: String,
        password: String
    ): LiveData<Result<LoginResponse>> {
        return authRepository.login(email, password)
    }

    fun getAuthenticatedUser(): LiveData<UserModel> {
        return authRepository.getPref().getAuthenticatedUser().asLiveData()
    }

}