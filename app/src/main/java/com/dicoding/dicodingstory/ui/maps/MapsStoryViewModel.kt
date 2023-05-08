package com.dicoding.dicodingstory.ui.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.dicoding.dicodingstory.data.Result
import com.dicoding.dicodingstory.data.models.UserModel
import com.dicoding.dicodingstory.data.repository.StoryRepository
import com.dicoding.dicodingstory.data.response.StoriesResponse

class MapsStoryViewModel(private val storyRepository: StoryRepository) : ViewModel() {
    suspend fun getStories(
        token: String,
        page: Int,
        size: Int,
        location: Int
    ): LiveData<Result<StoriesResponse>> {
        return storyRepository.getStories("Bearer $token", page, size, location)
    }

    fun getAuthenticatedUser(): LiveData<UserModel> {
        return storyRepository.getPref().getAuthenticatedUser().asLiveData()
    }
}