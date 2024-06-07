package com.firmannurcahyo.submission.frontend.model

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.firmannurcahyo.submission.database.datamodel.MapViewModel
import com.firmannurcahyo.submission.database.datamodel.UserPreferences
import com.firmannurcahyo.submission.frontend.StoriesViewModel
import com.firmannurcahyo.submission.frontend.authentication.LoginViewModel

class ViewModelFactory(private val pref: UserPreferences) : ViewModelProvider.NewInstanceFactory() {

    private lateinit var mApplication: Application

    fun setApplication(application: Application) {
        mApplication = application
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(pref) as T
            }

            modelClass.isAssignableFrom(StoriesViewModel::class.java) -> {
                StoriesViewModel(pref) as T
            }

            modelClass.isAssignableFrom(MapViewModel::class.java) -> {
                MapViewModel(pref, mApplication) as T
            }

            else -> {
                throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
            }
        }
    }
}