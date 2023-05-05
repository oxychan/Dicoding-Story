package com.dicoding.dicodingstory.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dicoding.dicodingstory.data.models.UserModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LoginPreference private constructor(private val dataStore: DataStore<Preferences>) {

    fun getAuthenticatedUser(): Flow<UserModel> {
        return dataStore.data.map { preferences ->
            UserModel(
                preferences[NAME_KEY] ?: "",
                preferences[TOKEN_KEY] ?: "",
                preferences[IS_AUTHENTICATED_KEY] ?: false
            )
        }
    }

    suspend fun saveUserLogin(userModel: UserModel) {
        dataStore.edit { preferences ->
            preferences[NAME_KEY] = userModel.name
            preferences[TOKEN_KEY] = userModel.token
            preferences[IS_AUTHENTICATED_KEY] = userModel.isAuthenticated
        }
    }

    suspend fun deleteUserLogin(): Boolean {
        try {
            dataStore.edit { preferences ->
                preferences[NAME_KEY] = ""
                preferences[TOKEN_KEY] = ""
                preferences[IS_AUTHENTICATED_KEY] = false
            }
        } catch (e: Exception) {
            return false
        }

        return true
    }

    companion object {
        @Volatile
        private var instance: LoginPreference? = null

        private val IS_AUTHENTICATED_KEY = booleanPreferencesKey("isAuthenticated")
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val NAME_KEY = stringPreferencesKey("name")

        fun getInstance(dataStore: DataStore<Preferences>): LoginPreference? {
            return instance ?: synchronized(this) {
                val inst = LoginPreference(dataStore)
                instance = inst
                instance
            }
        }
    }
}