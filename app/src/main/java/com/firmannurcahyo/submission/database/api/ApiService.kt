package com.firmannurcahyo.submission.database.api

import com.firmannurcahyo.submission.database.datamodel.BaseResponse
import com.firmannurcahyo.submission.database.datamodel.LoginRequest
import com.firmannurcahyo.submission.database.datamodel.LoginResponse
import com.firmannurcahyo.submission.database.datamodel.RegisterRequest
import com.firmannurcahyo.submission.database.datamodel.StoriesResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {
    @POST("register")
    fun register(@Body request: RegisterRequest): Call<BaseResponse>

    @POST("login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @GET("stories")
    suspend fun getStories(
        @Header("Authorization") token: String,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("location") location: Int? = null
    ): StoriesResponse

    @GET("stories")
    fun getStoriesLocation(
        @Header("Authorization") token: String,
        @Query("location") location: Int = 1,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): Call<StoriesResponse>

    @Multipart
    @POST("stories")
    fun addStories(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody
    ): Call<BaseResponse>

    @Multipart
    @POST("stories/guest")
    fun addGuestStories(
        @Part file: MultipartBody.Part, @Part("description") description: RequestBody
    ): Call<BaseResponse>
}
