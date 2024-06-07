package com.firmannurcahyo.submission.database.datamodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.firmannurcahyo.submission.database.api.ApiConfig
import com.firmannurcahyo.submission.database.datapaging.StoryDao
import com.firmannurcahyo.submission.database.datapaging.StoryDatabase
import com.firmannurcahyo.submission.frontend.authentication.Resource
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapViewModel(private val preferences: UserPreferences, application: Application) :
    ViewModel() {

    private var storyDao: StoryDao? = null
    private var storyDatabase: StoryDatabase? = StoryDatabase.getDatabase(application)

    private val _stories = MutableLiveData<Resource<ArrayList<StoriesDatabase>>>()
    val stories: LiveData<Resource<ArrayList<StoriesDatabase>>> = _stories

    init {
        storyDao = storyDatabase?.storyDao()
    }

    suspend fun getStories() {
        _stories.postValue(Resource.Loading())
        val client = ApiConfig.apiInstance.getStoriesLocation(
            token = "Bearer ${
                preferences.getUserKey().first()
            }"
        )

        client.enqueue(object : Callback<StoriesResponse> {
            override fun onResponse(
                call: Call<StoriesResponse>, response: Response<StoriesResponse>
            ) {
                when {
                    response.isSuccessful -> {
                        response.body()?.let {
                            val listStories = it.listStory
                            _stories.postValue(Resource.Success(ArrayList(listStories)))
                        }
                    }

                    else -> {
                        val errorResponse = Gson().fromJson(
                            response.errorBody()?.charStream(), BaseResponse::class.java
                        )
                        _stories.postValue(Resource.Error(errorResponse.message))
                    }
                }
            }

            override fun onFailure(call: Call<StoriesResponse>, t: Throwable) {
                _stories.postValue(Resource.Error(t.message))
            }
        })
    }
}