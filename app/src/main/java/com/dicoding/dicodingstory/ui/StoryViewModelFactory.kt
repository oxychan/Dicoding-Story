package com.dicoding.dicodingstory.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.dicodingstory.data.repository.StoryRepository
import com.dicoding.dicodingstory.di.Injection
import com.dicoding.dicodingstory.ui.dashboard.MainViewModel
import com.dicoding.dicodingstory.ui.maps.MapsStoryViewModel
import com.dicoding.dicodingstory.ui.story.StoryViewModel

class StoryViewModelFactory private constructor(
    private val storyRepository: StoryRepository
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(storyRepository) as T
        } else if (modelClass.isAssignableFrom(StoryViewModel::class.java)) {
            return StoryViewModel(storyRepository) as T
        } else if (modelClass.isAssignableFrom(MapsStoryViewModel::class.java)) {
            return MapsStoryViewModel(storyRepository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }

    companion object {
        @Volatile
        private var instance: StoryViewModelFactory? = null
        fun getInstance(context: Context): StoryViewModelFactory =
            instance ?: synchronized(this) {
                instance ?: StoryViewModelFactory(Injection.storyProviderRepository(context))
            }.also { instance = it }
    }
}