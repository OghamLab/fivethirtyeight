package com.ola.fivethirtyeight.remoteMediator
import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import com.ola.fivethirtyeight.dao.FeedItemDao
import com.ola.fivethirtyeight.dataSource.TopStoriesDataSource
import com.ola.fivethirtyeight.database.NewsDatabase
import com.ola.fivethirtyeight.datastore.SyncPreferences
import com.ola.fivethirtyeight.model.FeedItem
import com.ola.fivethirtyeight.model.FeedItemEntity
import com.ola.fivethirtyeight.model.toEntity
import kotlinx.coroutines.delay


@OptIn(ExperimentalPagingApi::class)
class TopStoriesRemoteMediator(
    private val dataSource: TopStoriesDataSource,
    private val dao: FeedItemDao,
    private val db: NewsDatabase,
    private val syncPreferences: SyncPreferences,
    private val maxRetries: Int = 3
) : RemoteMediator<Int, FeedItemEntity>() {

    private val tag = "TopStoriesMediator"

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, FeedItemEntity>
    ): MediatorResult {
        Log.d(tag, "load() called with loadType=$loadType")

        if (loadType != LoadType.REFRESH) {
            Log.d(tag, "Ignoring $loadType, treating as endOfPagination")
            return MediatorResult.Success(endOfPaginationReached = true)
        }



        var attempt = 0
        var lastError: Throwable? = null

        while (attempt < maxRetries) {
            try {
                attempt++
                Log.d(tag, "Attempt $attempt/$maxRetries")

                val remoteItems = fetchRemote()
                Log.d(tag, "Fetched ${remoteItems.size} items from network")

                val saved = dao.getSavedItemsOnce().associateBy { it.link }
                Log.d(tag, "Loaded ${saved.size} saved items from DB")

                val entities = remoteItems.map { item ->
                    val wasSaved = saved[item.link]?.isSavedForLater ?: false
                    item.toEntity().copy(isSavedForLater = wasSaved)
                }

                db.withTransaction {
                    Log.d(tag, "Clearing non-saved items and inserting ${entities.size} entities")
                    dao.clearAllNonSaved()
                    dao.upsertAll(entities)
                }

                syncPreferences.setLastSyncTime(System.currentTimeMillis())
                Log.d(tag, "Sync complete, DB updated, lastSyncTime set")

                return MediatorResult.Success(endOfPaginationReached = true)

            } catch (e: Exception) {
                lastError = e
                Log.e(tag, "Attempt $attempt failed", e)

                if (attempt >= maxRetries) {
                    Log.e(tag, "Max retries reached, giving up")
                    return MediatorResult.Error(e)
                }

                val backoff = attempt * 1000L
                Log.d(tag, "Retrying after ${backoff}ms")
                delay(backoff)
            }
        }

        return MediatorResult.Error(lastError ?: IllegalStateException("Unknown error"))
    }



    private suspend fun fetchRemote(): List<FeedItem> {
        val items = dataSource.fetchAllFeeds()

        return dataSource.concatenate(
            items,
            onlyRecentMillis = 172800000 // 48h
        )
    }


}


suspend fun <R> FeedItemDao.withTransaction(block: suspend () -> R): R =
    (this as RoomDatabase).withTransaction { block() }


