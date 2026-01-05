
package com.ola.fivethirtyeight.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ola.fivethirtyeight.model.SportsItemEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface SportsItemDao: BaseFeedDao<SportsItemEntity> {

    /** UI: observe all feeds */
    @Query("SELECT * FROM sports_items ORDER BY timeInMil DESC")
    override fun getAllFeeds(): Flow<List<SportsItemEntity>>


    /** UI: observe a single feed item by link */
    //@Query("SELECT * FROM sports_items WHERE link = :link ORDER BY timeInMil DESC")
   // override fun getFeedsBySource(link: String): Flow<SportsItemEntity?>

    @Query("SELECT * FROM sports_items WHERE link = :link LIMIT 1")
    override fun getItemByLink(link: String): Flow<SportsItemEntity?>
    /** UI: observe saved items */
    @Query("SELECT * FROM sports_items WHERE isSavedForLater = 1 ORDER BY publishedAt DESC")
    override fun getSavedItems(): Flow<List<SportsItemEntity>>


    /** Background sync / notifications: one-time snapshot */
    @Query("SELECT * FROM sports_items ORDER BY timeInMil DESC")
    override suspend fun getAllItemsSorted(): List<SportsItemEntity>


    /** Background sync: one-time saved snapshot */
    @Query("SELECT * FROM sports_items WHERE isSavedForLater = 1")
    override suspend fun getSavedItemsOnce(): List<SportsItemEntity>


    /* ---------- WRITES ---------- */

    /** Update saved flag */
    @Query("UPDATE sports_items SET isSavedForLater = :saved WHERE link = :link")
    override suspend fun updateSavedStatus(link: String, saved: Boolean)

    /** Background sync: remove non-saved items */
    @Query("DELETE FROM sports_items WHERE isSavedForLater = 0")
    override suspend fun clearAllNonSaved()


    /** Background sync: upsert fresh feed */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun upsertAll(items: List<SportsItemEntity>)


}
