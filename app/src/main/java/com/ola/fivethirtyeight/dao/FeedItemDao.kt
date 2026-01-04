
package com.ola.fivethirtyeight.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ola.fivethirtyeight.model.FeedItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedItemDao: BaseFeedDao<FeedItemEntity> {

    /** UI: observe all feeds */
    @Query("SELECT * FROM feed_items ORDER BY timeInMil DESC")
 override fun getAllFeeds(): Flow<List<FeedItemEntity>>


    /** UI: observe a single feed item by link */
    @Query("SELECT * FROM feed_items WHERE link = :link LIMIT 1")
    override fun getFeedsBySource(link: String): Flow<List<FeedItemEntity>>


    /** UI: observe saved items */
    @Query("SELECT * FROM feed_items WHERE isSavedForLater = 1 ORDER BY publishedAt DESC")
    override fun getSavedItems(): Flow<List<FeedItemEntity>>


    /** Background sync / notifications: one-time snapshot */
    @Query("SELECT * FROM feed_items ORDER BY timeInMil DESC")
    override suspend fun getAllItemsSorted(): List<FeedItemEntity>


    /** Background sync: one-time saved snapshot */
    @Query("SELECT * FROM feed_items WHERE isSavedForLater = 1")
    override suspend fun getSavedItemsOnce(): List<FeedItemEntity>


    /* ---------- WRITES ---------- */

    /** Update saved flag */
    @Query("UPDATE feed_items SET isSavedForLater = :saved WHERE link = :link")
    override suspend fun updateSavedStatus(link: String, saved: Boolean)

    /** Background sync: remove non-saved items */
    @Query("DELETE FROM feed_items WHERE isSavedForLater = 0")
    override suspend fun clearAllNonSaved()


    /** Background sync: upsert fresh feed */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun upsertAll(items: List<FeedItemEntity>)





      @Query(
         """
        SELECT * FROM feed_items
        ORDER BY timeInMil DESC
    """
      )
      fun pagingSource(): PagingSource<Int, FeedItemEntity>


   }








