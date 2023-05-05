package com.dicoding.dicodingstory.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.dicoding.dicodingstory.data.Result
import com.dicoding.dicodingstory.data.local.LoginPreference
import com.dicoding.dicodingstory.data.models.UserModel
import com.dicoding.dicodingstory.data.response.LoginResponse
import com.dicoding.dicodingstory.data.response.RegisterResponse
import com.dicoding.dicodingstory.data.retrofit.ApiService

class AuthRepository private constructor(
    private val apiService: ApiService,
    private val loginPreference: LoginPreference
) {
    // fun to login
    fun login(
        email: String,
        password: String
    ): LiveData<Result<LoginResponse>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.login(email, password)
            if (response.loginResult != null) {
                val name = response.loginResult.name.toString()
                val token = response.loginResult.token.toString()

                Log.d("LoginNih", "$name and $token")

                loginPreference.saveUserLogin(
                    UserModel(
                        name = name,
                        token = token,
                        isAuthenticated = true
                    )
                )
            }
            emit(Result.Success(response))
        } catch (e: Exception) {
            emit(Result.Error("login gagal: ${e.message}"))
        }
    }

    // fun to regist
    suspend fun register(
        name: String,
        email: String,
        password: String
    ): LiveData<Result<RegisterResponse>> =
        liveData {
            emit(Result.Loading)
            try {
                val response = apiService.register(name, email, password)
                emit(Result.Success(response))

            } catch (e: Exception) {
                emit(Result.Error("Registrasi gagal: $e"))
            }
        }

    fun getPref() = loginPreference

    companion object {
        @Volatile
        private var instance: AuthRepository? = null
        fun getInstance(
            apiService: ApiService,
            loginPreference: LoginPreference
        ): AuthRepository = instance ?: synchronized(this) {
            instance ?: AuthRepository(apiService, loginPreference)
        }.also { instance = it }
    }
}