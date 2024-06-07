package com.firmannurcahyo.submission.database.datapaging

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.firmannurcahyo.submission.database.datamodel.StoriesDatabase

@Dao
interface StoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stories: StoriesDatabase)

    @Query("DELETE FROM stories")
    suspend fun deleteAll(): Int

    @Query("SELECT * FROM stories")
    fun findAll(): PagingSource<Int, StoriesDatabase>
}