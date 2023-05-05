package com.dicoding.dicodingstory.data.retrofit

import com.dicoding.dicodingstory.data.response.LoginResponse
import com.dicoding.dicodingstory.data.response.RegisterResponse
import com.dicoding.dicodingstory.data.response.StoriesResponse
import com.dicoding.dicodingstory.data.response.StoryResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @POST("register")
    @FormUrlEncoded
    suspend fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): RegisterResponse

    @POST("login")
    @FormUrlEncoded
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): LoginResponse

    @GET("stories")
    suspend fun getStories(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("location") location: Int
    ): StoriesResponse

    @Multipart
    @POST("stories")
    suspend fun postStory(
        @Header("Authorization") token: String,
        @Part("description") description: RequestBody,
        @Part file: MultipartBody.Part
    ): StoryResponse
}