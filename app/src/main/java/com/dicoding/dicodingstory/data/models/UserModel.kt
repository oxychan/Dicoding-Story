package com.dicoding.dicodingstory.data.models

data class UserModel(
    val name: String,
    val token: String,
    val isAuthenticated: Boolean
)