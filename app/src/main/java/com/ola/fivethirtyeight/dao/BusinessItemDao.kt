
package com.ola.fivethirtyeight.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ola.fivethirtyeight.model.BusinessItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessItemDao: BaseFeedDao<BusinessItemEntity> {

    /** UI: observe all feeds */
    @Query("SELECT * FROM bus_items ORDER BY timeInMil DESC")
    override fun getAllFeeds(): Flow<List<BusinessItemEntity>>


    /** UI: observe a single feed item by link */
    //@Query("SELECT * FROM bus_items WHERE link = :link ORDER BY timeInMil DESC")
   // override fun getFeedsBySource(link: String): Flow<BusinessItemEntity?>

    @Query("SELECT * FROM bus_items WHERE link = :link LIMIT 1")
    override fun getItemByLink(link: String): Flow<BusinessItemEntity?>
    /** UI: observe saved items */
    @Query("SELECT * FROM bus_items WHERE isSavedForLater = 1 ORDER BY publishedAt DESC")
    override fun getSavedItems(): Flow<List<BusinessItemEntity>>


    /** Background sync / notifications: one-time snapshot */
    @Query("SELECT * FROM bus_items ORDER BY timeInMil DESC")
    override suspend fun getAllItemsSorted(): List<BusinessItemEntity>


    /** Background sync: one-time saved snapshot */
    @Query("SELECT * FROM bus_items WHERE isSavedForLater = 1")
    override suspend fun getSavedItemsOnce(): List<BusinessItemEntity>


    /* ---------- WRITES ---------- */

    /** Update saved flag */
    @Query("UPDATE bus_items SET isSavedForLater = :saved WHERE link = :link")
    override suspend fun updateSavedStatus(link: String, saved: Boolean)

    /** Background sync: remove non-saved items */
    @Query("DELETE FROM bus_items WHERE isSavedForLater = 0")
    override suspend fun clearAllNonSaved()


    /** Background sync: upsert fresh feed */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun upsertAll(items: List<BusinessItemEntity>)


}
