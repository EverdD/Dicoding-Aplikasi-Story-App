package com.firmannurcahyo.submission.frontend.model

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.firmannurcahyo.submission.database.api.ApiConfig
import com.firmannurcahyo.submission.database.datamodel.UserPreferences
import com.firmannurcahyo.submission.database.datapaging.StoryDatabase
import com.firmannurcahyo.submission.database.datapaging.StoryRepository

object Injection {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_key")
    fun provideRepository(context: Context): StoryRepository {
        val database = StoryDatabase.getDatabase(context)
        val apiService = ApiConfig.apiInstance
        val pref = UserPreferences.getInstance(context.dataStore)
        return StoryRepository(database, apiService, pref)
    }
}