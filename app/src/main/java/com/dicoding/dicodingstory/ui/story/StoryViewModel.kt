package com.dicoding.dicodingstory.ui.story

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.dicoding.dicodingstory.data.Result
import com.dicoding.dicodingstory.data.models.UserModel
import com.dicoding.dicodingstory.data.repository.StoryRepository
import com.dicoding.dicodingstory.data.response.StoryResponse
import java.io.File

class StoryViewModel(private val storyRepository: StoryRepository) : ViewModel() {
    suspend fun postStory(
        token: String,
        description: String,
        file: File?
    ): LiveData<Result<StoryResponse>> {
        return storyRepository.postStory(
            token = "Bearer $token",
            description = description,
            file = file
        )
    }

    fun getAuthenticatedUser(): LiveData<UserModel> {
        return storyRepository.getPref().getAuthenticatedUser().asLiveData()
    }
}