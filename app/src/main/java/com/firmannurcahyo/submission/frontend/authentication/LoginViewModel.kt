package com.firmannurcahyo.submission.frontend.authentication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.firmannurcahyo.submission.database.api.ApiConfig
import com.firmannurcahyo.submission.database.datamodel.BaseResponse
import com.firmannurcahyo.submission.database.datamodel.LoginRequest
import com.firmannurcahyo.submission.database.datamodel.LoginResponse
import com.firmannurcahyo.submission.database.datamodel.RegisterRequest
import com.firmannurcahyo.submission.database.datamodel.UserPreferences
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel(private val preferences: UserPreferences) : ViewModel() {

    private val _authInfo = MutableLiveData<Resource<String>>()
    val authInfo: LiveData<Resource<String>> = _authInfo

    fun login(email: String, password: String) {
        _authInfo.postValue(Resource.Loading())
        val client = ApiConfig.apiInstance.login(LoginRequest(email, password))

        client.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                when {
                    response.isSuccessful -> {
                        val loginResult = response.body()?.loginResult?.token
                        loginResult?.let { saveUserKey(it) }
                        _authInfo.postValue(Resource.Success(loginResult))
                    }

                    else -> handleErrorResponse(response.errorBody())
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                _authInfo.postValue(Resource.Error(t.message))
            }
        })
    }

    fun register(name: String, email: String, password: String) {
        _authInfo.postValue(Resource.Loading())
        val client = ApiConfig.apiInstance.register(RegisterRequest(name, email, password))

        client.enqueue(object : Callback<BaseResponse> {
            override fun onResponse(call: Call<BaseResponse>, response: Response<BaseResponse>) {
                when {
                    response.isSuccessful -> {
                        val message = response.body()?.message.toString()
                        _authInfo.postValue(Resource.Success(message))
                    }

                    else -> handleErrorResponse(response.errorBody())
                }
            }

            override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
                _authInfo.postValue(Resource.Error(t.message))
            }
        })
    }

    fun logout() = deleteUserKey()

    fun getUserKey() = preferences.getUserKey().asLiveData()

    private fun saveUserKey(key: String) {
        viewModelScope.launch {
            preferences.saveUserKey(key)
        }
    }

    private fun deleteUserKey() {
        viewModelScope.launch {
            preferences.deleteUserKey()
        }
    }

    private fun handleErrorResponse(errorBody: ResponseBody?) {
        val errorResponse = Gson().fromJson(errorBody?.charStream(), BaseResponse::class.java)
        _authInfo.postValue(Resource.Error(errorResponse.message))
    }
}