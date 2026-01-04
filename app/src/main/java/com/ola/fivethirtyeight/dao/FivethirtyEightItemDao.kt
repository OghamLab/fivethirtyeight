
package com.ola.fivethirtyeight.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ola.fivethirtyeight.model.FiveThirtyEightItemEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface FiveThirtyEightItemDao: BaseFeedDao<FiveThirtyEightItemEntity> {

    /** UI: observe all feeds */
    @Query("SELECT * FROM fiveThirtyEight_items ORDER BY timeInMil DESC")
    override fun getAllFeeds(): Flow<List<FiveThirtyEightItemEntity>>


    /** UI: observe a single feed item by link */
    @Query("SELECT * FROM fiveThirtyEight_items WHERE link = :link ORDER BY timeInMil DESC")
    override fun getFeedsBySource(link: String): Flow<List<FiveThirtyEightItemEntity>>


    /** UI: observe saved items */
    @Query("SELECT * FROM fiveThirtyEight_items WHERE isSavedForLater = 1 ORDER BY publishedAt DESC")
    override fun getSavedItems(): Flow<List<FiveThirtyEightItemEntity>>


    /** Background sync / notifications: one-time snapshot */
    @Query("SELECT * FROM fiveThirtyEight_items ORDER BY timeInMil DESC")
    override suspend fun getAllItemsSorted(): List<FiveThirtyEightItemEntity>


    /** Background sync: one-time saved snapshot */
    @Query("SELECT * FROM fiveThirtyEight_items WHERE isSavedForLater = 1")
    override suspend fun getSavedItemsOnce(): List<FiveThirtyEightItemEntity>


    /* ---------- WRITES ---------- */

    /** Update saved flag */
    @Query("UPDATE fiveThirtyEight_items SET isSavedForLater = :saved WHERE link = :link")
    override suspend fun updateSavedStatus(link: String, saved: Boolean)

    /** Background sync: remove non-saved items */
    @Query("DELETE FROM fiveThirtyEight_items WHERE isSavedForLater = 0")
    override suspend fun clearAllNonSaved()


    /** Background sync: upsert fresh feed */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun upsertAll(items: List<FiveThirtyEightItemEntity>)


}
