package com.firmannurcahyo.submission.database.datapaging

import androidx.lifecycle.LiveData
import androidx.paging.*
import com.firmannurcahyo.submission.database.api.ApiService
import com.firmannurcahyo.submission.database.datamodel.StoriesDatabase
import com.firmannurcahyo.submission.database.datamodel.UserPreferences

class StoryRepository(
    private val storyDatabase: StoryDatabase,
    private val apiService: ApiService,
    private val token: UserPreferences
) {
    @OptIn(ExperimentalPagingApi::class)
    fun getStories(): LiveData<PagingData<StoriesDatabase>> {
        val pager = Pager(
            config = PagingConfig(
                pageSize = 3
            ),
            remoteMediator = StoryRemoteMediator(storyDatabase, apiService, token),
            pagingSourceFactory = { storyDatabase.storyDao().findAll() }
        )
        return when {
            pager.liveData != null -> pager.liveData
            else -> throw IllegalStateException("LiveData cannot be null")
        }
    }
}