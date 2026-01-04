
package com.ola.fivethirtyeight.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ola.fivethirtyeight.model.WorldItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorldItemDao: BaseFeedDao<WorldItemEntity> {

    /** UI: observe all feeds */
    @Query("SELECT * FROM world_items ORDER BY timeInMil DESC")
    override fun getAllFeeds(): Flow<List<WorldItemEntity>>


    /** UI: observe a single feed item by link */
    @Query("SELECT * FROM world_items WHERE link = :link ORDER BY timeInMil DESC")
    override fun getFeedsBySource(link: String): Flow<List<WorldItemEntity>>


    /** UI: observe saved items */
    @Query("SELECT * FROM world_items WHERE isSavedForLater = 1 ORDER BY publishedAt DESC")
    override fun getSavedItems(): Flow<List<WorldItemEntity>>


    /** Background sync / notifications: one-time snapshot */
    @Query("SELECT * FROM world_items ORDER BY timeInMil DESC")
    override suspend fun getAllItemsSorted(): List<WorldItemEntity>


    /** Background sync: one-time saved snapshot */
    @Query("SELECT * FROM world_items WHERE isSavedForLater = 1")
    override suspend fun getSavedItemsOnce(): List<WorldItemEntity>


    /* ---------- WRITES ---------- */

    /** Update saved flag */
    @Query("UPDATE world_items SET isSavedForLater = :saved WHERE link = :link")
    override suspend fun updateSavedStatus(link: String, saved: Boolean)

    /** Background sync: remove non-saved items */
    @Query("DELETE FROM world_items WHERE isSavedForLater = 0")
    override suspend fun clearAllNonSaved()


    /** Background sync: upsert fresh feed */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun upsertAll(items: List<WorldItemEntity>)


}