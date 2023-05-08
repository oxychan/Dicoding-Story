package com.dicoding.dicodingstory

import com.dicoding.dicodingstory.data.models.StoryModel
import com.dicoding.dicodingstory.data.response.Story
import com.dicoding.dicodingstory.data.response.StoryResponse
import com.google.gson.annotations.SerializedName

object DataDummy {
    fun generateDummyStoryResponse(): List<Story> {
        val items: MutableList<Story> = arrayListOf()
        for (i in 0..100) {
            val story = Story(
                id = i.toString(),
                name = "name $i",
                photoUrl = "photo $i",
                description = "desc $i",
                createdAt = "created $i",
                lat = "lat $i",
                lon = "lon $i",
            )
            items.add(story)
        }

        return items
    }
}