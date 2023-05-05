package com.dicoding.dicodingstory.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StoryModel(
    val name: String,
    val photoUrl: String,
    val description: String,
    val createdAt: String?
) : Parcelable