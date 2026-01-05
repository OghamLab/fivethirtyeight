package com.ola.fivethirtyeight.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ola.fivethirtyeight.model.FeedRefreshEntity

@Dao
interface FeedRefreshDao {

    @Query("SELECT * FROM feed_refresh WHERE id = 0")
    suspend fun get(): FeedRefreshEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: FeedRefreshEntity)
}
