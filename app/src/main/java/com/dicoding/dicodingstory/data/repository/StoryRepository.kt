package com.dicoding.dicodingstory.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.dicoding.dicodingstory.data.Result
import com.dicoding.dicodingstory.data.local.LoginPreference
import com.dicoding.dicodingstory.data.response.StoriesResponse
import com.dicoding.dicodingstory.data.response.StoryResponse
import com.dicoding.dicodingstory.data.retrofit.ApiService
import com.dicoding.dicodingstory.utils.Utils
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class StoryRepository private constructor(
    private val apiService: ApiService,
    private val loginPreference: LoginPreference
) {
    // fun to get all stories
    suspend fun getStories(
        token: String,
        page: Int,
        size: Int,
        location: Int
    ): LiveData<Result<StoriesResponse>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.getStories(token, page, size, location)
            emit(Result.Success(response))
        } catch (e: Exception) {
            emit(Result.Error("Get stories gagal: ${e.message}"))
        }
    }

    // fun to upload story
    suspend fun postStory(
        token: String,
        description: String,
        file: File?
    ): LiveData<Result<StoryResponse>> = liveData {
        if (file != null) {
            val fileToUpload = Utils.reduceFileImage(file)

            val descToUpload = description.toRequestBody("text/plain".toMediaType())
            val requestImgFile = fileToUpload.asRequestBody("image/jpeg".toMediaType())
            val imgMultiPart: MultipartBody.Part = MultipartBody.Part.createFormData(
                "photo",
                file.name,
                requestImgFile
            )

            emit(Result.Loading)
            try {
                val response = apiService.postStory(
                    token = token,
                    description = descToUpload,
                    file = imgMultiPart
                )
                emit(Result.Success(response))
            } catch (e: Exception) {
                emit(Result.Error("Post story gagal: ${e.message}"))
            }
        } else {
            emit(Result.Error("Silakan masukan gambar terlebih dahulu"))
        }
    }

    fun getPref() = loginPreference

    companion object {
        @Volatile
        private var instance: StoryRepository? = null
        fun getInstance(
            apiService: ApiService,
            loginPreference: LoginPreference
        ): StoryRepository = instance ?: synchronized(this) {
            instance ?: StoryRepository(apiService, loginPreference)
        }.also { instance = it }
    }
}