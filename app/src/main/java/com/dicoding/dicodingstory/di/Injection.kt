package com.dicoding.dicodingstory.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.dicoding.dicodingstory.data.local.LoginPreference
import com.dicoding.dicodingstory.data.repository.AuthRepository
import com.dicoding.dicodingstory.data.repository.StoryRepository
import com.dicoding.dicodingstory.data.retrofit.ApiConfig

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_login")

object Injection {
    fun authProviderRepository(context: Context): AuthRepository {
        val apiService = ApiConfig.getApiService()
        val loginPreferences = LoginPreference.getInstance(context.dataStore)

        return AuthRepository.getInstance(apiService, loginPreferences!!)
    }

    fun storyProviderRepository(context: Context): StoryRepository {
        val apiService = ApiConfig.getApiService()
        val loginPreferences = LoginPreference.getInstance(context.dataStore)

        return StoryRepository.getInstance(apiService, loginPreferences!!)
    }
}