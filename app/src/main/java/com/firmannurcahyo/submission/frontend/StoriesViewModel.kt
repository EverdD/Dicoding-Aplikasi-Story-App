package com.firmannurcahyo.submission.frontend

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.firmannurcahyo.submission.frontend.authentication.Resource
import com.firmannurcahyo.submission.database.api.ApiConfig
import com.firmannurcahyo.submission.database.datamodel.BaseResponse
import com.firmannurcahyo.submission.database.datamodel.UserPreferences
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StoriesViewModel(private val preferences: UserPreferences): ViewModel() {

    private val _uploadInfo = MutableLiveData<Resource<String>>()
    val uploadInfo: LiveData<Resource<String>> = _uploadInfo

    suspend fun upload(
        imageMultipart: MultipartBody.Part,
        descrition: RequestBody,
        asGuest: Boolean = false,
    ) {
        _uploadInfo.postValue(Resource.Loading())
        val client = if (asGuest) ApiConfig.apiInstance.addGuestStories(
            imageMultipart,
            descrition,
        ) else ApiConfig.apiInstance.addStories(
            token = "Bearer ${preferences.getUserKey().first()}",
            imageMultipart,
            descrition
        )

        client.enqueue(object : Callback<BaseResponse> {
            override fun onResponse(call: Call<BaseResponse>, response: Response<BaseResponse>) {
                if (response.isSuccessful) {
                    _uploadInfo.postValue(Resource.Success(response.body()?.message))
                } else {
                    val errorResponse = Gson().fromJson(
                        response.errorBody()?.charStream(),
                        BaseResponse::class.java
                    )
                    _uploadInfo.postValue(Resource.Error(errorResponse.message))
                }
            }

            override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
                _uploadInfo.postValue(Resource.Error(t.message))
            }
        })
    }
}