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
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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

    private suspend fun fetchRemote(): List<FeedItem> = coroutineScope {
        val abc = async { dataSource.getFeedList() }
        val google = async { dataSource.getGoogleTop() }
        val ny = async { dataSource.getNyTop() }
        val npr = async { dataSource.getNprTop() }

        dataSource.concatenate(
            abc.await(),
            google.await(),
            ny.await(),
            npr.await(),
            onlyRecentMillis = 172800000
        )
    }
}


/*
@OptIn(ExperimentalPagingApi::class)
class TopStoriesRemoteMediator(
    private val dataSource: TopStoriesDataSource,
    private val dao: FeedItemDao,
    private val db: NewsDatabase,
    private val syncPreferences: SyncPreferences
) : RemoteMediator<Int, FeedItemEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, FeedItemEntity>
    ): MediatorResult {
        return try {
            if (loadType != LoadType.REFRESH)
                return MediatorResult.Success(endOfPaginationReached = true)

            val remoteItems = coroutineScope {
                val abc = async { dataSource.getFeedList() }
                val google = async { dataSource.getGoogleTop() }
                val ny = async { dataSource.getNyTop() }
                val npr = async { dataSource.getNprTop() }

                dataSource.concatenate(
                    abc.await(),
                    google.await(),
                    ny.await(),
                    npr.await(),
                    onlyRecentMillis = 172800000
                )
            }

            val saved = dao.getSavedItemsOnce().associateBy { it.link }

            val entities = remoteItems.map { item ->
                val wasSaved = saved[item.link]?.isSavedForLater ?: false
                item.toEntity().copy(isSavedForLater = wasSaved)
            }

            db.withTransaction {
                dao.clearAllNonSaved()
                dao.upsertAll(entities)
            }

            syncPreferences.setLastSyncTime(System.currentTimeMillis())

            MediatorResult.Success(endOfPaginationReached = true)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}*/

/*

@OptIn(ExperimentalPagingApi::class)
class TopStoriesRemoteMediator(
    private val dataSource: TopStoriesDataSource,
    private val dao: FeedItemDao,
    private val syncPreferences: SyncPreferences
) : RemoteMediator<Int, FeedItemEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, FeedItemEntity>
    ): MediatorResult {

        return try {
            if (loadType != LoadType.REFRESH)
                return MediatorResult.Success(endOfPaginationReached = true)

            // Fetch remote feeds concurrently
            val remoteItems = coroutineScope {
                val abc = async { dataSource.getFeedList() }
                val google = async { dataSource.getGoogleTop() }
                val ny = async { dataSource.getNyTop() }
                val npr = async { dataSource.getNprTop() }

                dataSource.concatenate(
                    abc.await(),
                    google.await(),
                    ny.await(),
                    npr.await(),
                    onlyRecentMillis = 172800000
                )
            }

            // Preserve saved state
            val saved = dao.getSavedItemsOnce().associateBy { it.link }

            val entities = remoteItems.map { item ->
                val wasSaved = saved[item.link]?.isSavedForLater ?: false
                item.toEntity().copy(isSavedForLater = wasSaved)
            }

            // Atomic DB update
            dao.withTransaction {
                dao.clearAllNonSaved()
                dao.upsertAll(entities)
            }

            syncPreferences.setLastSyncTime(System.currentTimeMillis())

            MediatorResult.Success(endOfPaginationReached = true)

        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}

*/

/*

@OptIn(ExperimentalPagingApi::class)
class TopStoriesRemoteMediator(
    private val dataSource: TopStoriesDataSource,
    private val dao: FeedItemDao,
    private val syncPreferences: SyncPreferences
) : RemoteMediator<Int, FeedItemEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, FeedItemEntity>
    ): MediatorResult {

        return try {
            // Only refresh; no prepend/append for top stories
            if (loadType != LoadType.REFRESH)
                return MediatorResult.Success(endOfPaginationReached = true)

            // Fetch remote feeds concurrently
            val remoteItems = coroutineScope {
                val abc = async { dataSource.getFeedList() }
                val google = async { dataSource.getGoogleTop() }
                val ny = async { dataSource.getNyTop() }
                val npr = async { dataSource.getNprTop() }

                dataSource.concatenate(
                    abc.await(),
                    google.await(),
                    ny.await(),
                    npr.await(),
                    onlyRecentMillis = 172800000
                )
            }

            // Preserve saved state
            val saved = dao.getSavedItemsOnce().associateBy { it.link }

            val entities = remoteItems.map { item ->
                val wasSaved = saved[item.link]?.isSavedForLater ?: false
                item.toEntity().copy(isSavedForLater = wasSaved)
            }

            // Atomic DB update
            dao.withTransaction {
                dao.clearAllNonSaved()
                dao.upsertAll(entities)
            }

            // Update sync timestamp
            syncPreferences.setLastSyncTime(System.currentTimeMillis())

            MediatorResult.Success(endOfPaginationReached = true)

        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}
*/


/*

@OptIn(ExperimentalPagingApi::class)
class TopStoriesRemoteMediator(
    private val dataSource: TopStoriesDataSource,
    private val dao: FeedItemDao
) : RemoteMediator<Int, FeedItemEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, FeedItemEntity>
    ): MediatorResult {

        return try {
            // Only refresh on first load
            if (loadType == LoadType.PREPEND) return MediatorResult.Success(endOfPaginationReached = true)
            if (loadType == LoadType.APPEND) return MediatorResult.Success(endOfPaginationReached = true)

            // Fetch remote
            val remoteItems = coroutineScope {
                val abc = async { dataSource.getFeedList() }
                val google = async { dataSource.getGoogleTop() }
                val ny = async { dataSource.getNyTop() }
                val npr = async { dataSource.getNprTop() }

                dataSource.concatenate(
                    abc.await(),
                    google.await(),
                    ny.await(),
                    npr.await(),
                    onlyRecentMillis = 172800000
                )
            }

            // Convert to entities
            val entities = remoteItems.map { it.toEntity() }

            // Write to DB atomically
            dao.withTransaction {
                dao.clearAllNonSaved()
                dao.upsertAll(entities)
            }

            MediatorResult.Success(endOfPaginationReached = true)

        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}
*/


suspend fun <R> FeedItemDao.withTransaction(block: suspend () -> R): R =
    (this as RoomDatabase).withTransaction { block() }


/*


@OptIn(ExperimentalPagingApi::class)
class TopStoriesRemoteMediator(
    private val dataSource: TopStoriesDataSource,
    private val feedItemDao: FeedItemDao,
    private val refreshDao: FeedRefreshDao,
    private val database: NewsDatabase,
    private val refreshIntervalMs: Long = 30 * 60 * 1000L // 30 minutes
) : RemoteMediator<Int, FeedItemEntity>() {

    override suspend fun initialize(): InitializeAction {
        val lastRefresh = refreshDao.get()?.lastRefreshTime ?: 0L
        return if (System.currentTimeMillis() - lastRefresh > refreshIntervalMs) {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else {
            InitializeAction.SKIP_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, FeedItemEntity>
    ): MediatorResult {

        // No prepend in RSS-style feeds
        if (loadType == LoadType.PREPEND) {
            return MediatorResult.Success(endOfPaginationReached = true)
        }

        return try {
            val remoteItems = coroutineScope {
                val abc = async { dataSource.getFeedList() }
                val google = async { dataSource.getGoogleTop() }
                val ny = async { dataSource.getNyTop() }
                val npr = async { dataSource.getNprTop() }

                dataSource.concatenate(
                    abc.await(),
                    google.await(),
                    ny.await(),
                    npr.await(),
                    onlyRecentMillis = 172800000 // 48h
                )
            }

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    feedItemDao.clearAll() // 🔑 full table replace
                }

                feedItemDao.upsertAll(
                    remoteItems.map { it.toEntity() }
                )

                refreshDao.upsert(
                    FeedRefreshEntity(
                        lastRefreshTime = System.currentTimeMillis()
                    )
                )
            }

            MediatorResult.Success( true)

        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}
*/
