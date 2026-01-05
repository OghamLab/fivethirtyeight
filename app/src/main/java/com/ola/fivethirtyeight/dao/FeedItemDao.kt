
package com.ola.fivethirtyeight.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ola.fivethirtyeight.model.FeedItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedItemDao : BaseFeedDao<FeedItemEntity> {

    /* ---------- Paging ---------- */
    @Query("SELECT * FROM feed_items ORDER BY timeInMil DESC")
    fun pagingSource(): PagingSource<Int, FeedItemEntity>

    /* ---------- Flows ---------- */
    @Query("SELECT * FROM feed_items ORDER BY timeInMil DESC")
    override fun getAllFeeds(): Flow<List<FeedItemEntity>>

    @Query("SELECT * FROM feed_items WHERE link = :link LIMIT 1")
    override fun getItemByLink(link: String): Flow<FeedItemEntity?>

    @Query("SELECT * FROM feed_items WHERE isSavedForLater = 1 ORDER BY publishedAt DESC")
    override fun getSavedItems(): Flow<List<FeedItemEntity>>

    /* ---------- Snapshots ---------- */
    @Query("SELECT * FROM feed_items ORDER BY timeInMil DESC")
    override suspend fun getAllItemsSorted(): List<FeedItemEntity>

    @Query("SELECT * FROM feed_items WHERE isSavedForLater = 1 ORDER BY publishedAt DESC")
    override suspend fun getSavedItemsOnce(): List<FeedItemEntity>

    /* ---------- Writes ---------- */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun upsertAll(items: List<FeedItemEntity>)

    @Query("UPDATE feed_items SET isSavedForLater = :saved WHERE link = :link")
    override suspend fun updateSavedStatus(link: String, saved: Boolean)

    @Query("DELETE FROM feed_items WHERE isSavedForLater = 0")
    override suspend fun clearAllNonSaved()


    @Query("DELETE FROM feed_items")
    suspend fun clearAll()


}




