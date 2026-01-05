
package com.ola.fivethirtyeight.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ola.fivethirtyeight.model.PoliticsItemEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface PoliticsItemDao: BaseFeedDao<PoliticsItemEntity> {

    /** UI: observe all feeds */
    @Query("SELECT * FROM politics_items ORDER BY timeInMil DESC")
    override fun getAllFeeds(): Flow<List<PoliticsItemEntity>>


    /** UI: observe a single feed item by link */
   // @Query("SELECT * FROM politics_items WHERE link = :link ORDER BY timeInMil DESC")
    //override fun getFeedsBySource(link: String): Flow<PoliticsItemEntity?>
    @Query("SELECT * FROM politics_items WHERE link = :link LIMIT 1")
    override fun getItemByLink(link: String): Flow<PoliticsItemEntity?>

    /** UI: observe saved items */
    @Query("SELECT * FROM politics_items WHERE isSavedForLater = 1 ORDER BY publishedAt DESC")
    override fun getSavedItems(): Flow<List<PoliticsItemEntity>>


    /** Background sync / notifications: one-time snapshot */
    @Query("SELECT * FROM politics_items ORDER BY timeInMil DESC")
    override suspend fun getAllItemsSorted(): List<PoliticsItemEntity>


    /** Background sync: one-time saved snapshot */
    @Query("SELECT * FROM politics_items WHERE isSavedForLater = 1")
    override suspend fun getSavedItemsOnce(): List<PoliticsItemEntity>


    /* ---------- WRITES ---------- */

    /** Update saved flag */
    @Query("UPDATE politics_items SET isSavedForLater = :saved WHERE link = :link")
    override suspend fun updateSavedStatus(link: String, saved: Boolean)

    /** Background sync: remove non-saved items */
    @Query("DELETE FROM politics_items WHERE isSavedForLater = 0")
    override suspend fun clearAllNonSaved()


    /** Background sync: upsert fresh feed */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun upsertAll(items: List<PoliticsItemEntity>)


}



