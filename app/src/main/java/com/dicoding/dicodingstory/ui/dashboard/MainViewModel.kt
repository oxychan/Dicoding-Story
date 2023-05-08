package com.dicoding.dicodingstory.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dicoding.dicodingstory.data.Result
import com.dicoding.dicodingstory.data.models.UserModel
import com.dicoding.dicodingstory.data.repository.StoryRepository
import com.dicoding.dicodingstory.data.response.StoriesResponse
import com.dicoding.dicodingstory.data.response.Story

class MainViewModel(private val storyRepository: StoryRepository) : ViewModel() {

    suspend fun getStories(
        token: String,
        page: Int,
        size: Int,
        location: Int
    ): LiveData<Result<StoriesResponse>> {
        return storyRepository.getStories("Bearer $token", page, size, location)
    }

    fun getStoriesWithPaging(): LiveData<PagingData<Story>> {
        return storyRepository.getStoriesWithPaging().cachedIn(viewModelScope)
    }

    fun getAuthenticatedUser(): LiveData<UserModel> {
        return storyRepository.getPref().getAuthenticatedUser().asLiveData()
    }

    suspend fun logout(): Boolean {
        return storyRepository.getPref().deleteUserLogin()
    }
}