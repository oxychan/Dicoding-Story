package com.dicoding.dicodingstory.data.response

import com.google.gson.annotations.SerializedName

// response for adding new story
data class StoryResponse(

    @field:SerializedName("error")
    val error: Boolean? = null,

    @field:SerializedName("message")
    val message: String? = null
)

// response for getting all stories data
data class StoriesResponse(

    @field:SerializedName("listStory")
    val listStory: List<Story?>? = null,

    @field:SerializedName("error")
    val error: Boolean? = null,

    @field:SerializedName("message")
    val message: String? = null
)

data class Story(

    @field:SerializedName("photoUrl")
    val photoUrl: String? = null,

    @field:SerializedName("createdAt")
    val createdAt: String? = null,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("description")
    val description: String? = null,

    @field:SerializedName("lon")
    val lon: Any? = null,

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("lat")
    val lat: Any? = null
)




