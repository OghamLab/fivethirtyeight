package com.ola.fivethirtyeight.remoteMediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.ola.fivethirtyeight.dao.FeedItemDao
import com.ola.fivethirtyeight.dao.FeedRefreshDao
import com.ola.fivethirtyeight.dataSource.TopStoriesDataSource
import com.ola.fivethirtyeight.database.NewsDatabase
import com.ola.fivethirtyeight.model.FeedItemEntity
import com.ola.fivethirtyeight.model.FeedRefreshEntity
import com.ola.fivethirtyeight.model.toEntity
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

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
