package com.firmannurcahyo.submission.frontend

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.firmannurcahyo.submission.database.datamodel.StoriesDatabase
import com.firmannurcahyo.submission.database.datapaging.StoryRepository

class MainViewModel(private val repository: StoryRepository) : ViewModel() {

    fun getStories(): LiveData<PagingData<StoriesDatabase>> =
        repository.getStories().cachedIn(viewModelScope)
}