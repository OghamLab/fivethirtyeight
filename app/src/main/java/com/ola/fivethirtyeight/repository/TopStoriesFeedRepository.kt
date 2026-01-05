package com.ola.fivethirtyeight.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.ola.fivethirtyeight.dao.FeedItemDao
import com.ola.fivethirtyeight.dataSource.TopStoriesDataSource
import com.ola.fivethirtyeight.database.NewsDatabase
import com.ola.fivethirtyeight.datastore.SyncPreferences
import com.ola.fivethirtyeight.model.FeedItem
import com.ola.fivethirtyeight.model.toFeedItem
import com.ola.fivethirtyeight.remoteMediator.TopStoriesRemoteMediator
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


@OptIn(ExperimentalPagingApi::class)
class TopStoriesFeedRepository @Inject constructor(
    private val dataSource: TopStoriesDataSource,
    private val dao: FeedItemDao,
    private val db: NewsDatabase,
    private val syncPreferences: SyncPreferences
) {

    fun pagingTopStories(): Flow<PagingData<FeedItem>> =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 40,
                prefetchDistance = 5,
                enablePlaceholders = false
            ),
            remoteMediator = TopStoriesRemoteMediator(
                dataSource = dataSource,
                dao = dao,
                db = db,
                syncPreferences = syncPreferences
            ),
            pagingSourceFactory = { dao.pagingSource() }
        )
            .flow
            .map { pagingData -> pagingData.map { it.toFeedItem() } }

    suspend fun workerSync(): List<FeedItem> {
        val remote = coroutineScope {
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

        val lastSync = syncPreferences.getLastSyncTime()
        val now = System.currentTimeMillis()

        val newItems = remote.filter { it.timeInMil > lastSync }

        syncPreferences.setLastSyncTime(now)

        return newItems
    }

    fun isArticleSaved(link: String): Flow<Boolean> =
        dao.getItemByLink(link).map { it?.isSavedForLater == true }

    fun getSavedArticles(): Flow<List<FeedItem>> =
        dao.getSavedItems().map { list -> list.map { it.toFeedItem() } }

    suspend fun toggleSave(link: String, save: Boolean) {
        dao.updateSavedStatus(link, save)
    }
}







/*

@OptIn(ExperimentalPagingApi::class)
class TopStoriesFeedRepository @Inject constructor(
    private val dataSource: TopStoriesDataSource,
    private val dao: FeedItemDao,
    private val syncPreferences: SyncPreferences
) {

    fun pagingTopStories(): Flow<PagingData<FeedItem>> =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 40,
                prefetchDistance = 5,
                enablePlaceholders = false
            ),
            remoteMediator = TopStoriesRemoteMediator(
                dataSource = dataSource,
                dao = dao,
                syncPreferences = syncPreferences
            ),
            pagingSourceFactory = { dao.pagingSource() }
        )
            .flow
            .map { it.map { entity -> entity.toFeedItem() } }

    // worker-only sync: no DB writes, just new items for notifications
    suspend fun workerSync(): List<FeedItem> {
        val remote = coroutineScope {
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

        val lastSync = syncPreferences.getLastSyncTime()
        val now = System.currentTimeMillis()

        val newItems = remote.filter { it.timeInMil > lastSync }

        syncPreferences.setLastSyncTime(now)

        return newItems
    }

    fun isArticleSaved(link: String): Flow<Boolean> =
        dao.getItemByLink(link).map { it?.isSavedForLater == true }

    fun getSavedArticles(): Flow<List<FeedItem>> =
        dao.getSavedItems().map { list -> list.map { it.toFeedItem() } }

    suspend fun toggleSave(link: String, save: Boolean) {
        dao.updateSavedStatus(link, save)
    }
}
*/


/*

@OptIn(ExperimentalPagingApi::class)
class TopStoriesFeedRepository @Inject constructor(
    private val dataSource: TopStoriesDataSource,
    private val dao: FeedItemDao,
    private val syncPreferences: SyncPreferences
) {

    fun pagingTopStories(): Flow<PagingData<FeedItem>> =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 40,
                prefetchDistance = 5,
                enablePlaceholders = false
            ),
            remoteMediator = TopStoriesRemoteMediator(
                dataSource = dataSource,
                dao = dao,
                syncPreferences = syncPreferences
            ),
            pagingSourceFactory = { dao.pagingSource() }
        )
            .flow
            .map { it.map { entity -> entity.toFeedItem() } }

    fun isArticleSaved(link: String): Flow<Boolean> =
        dao.getItemByLink(link).map { it?.isSavedForLater == true }

    fun getSavedArticles(): Flow<List<FeedItem>> =
        dao.getSavedItems().map { list -> list.map { it.toFeedItem() } }

    suspend fun toggleSave(link: String, save: Boolean) {
        dao.updateSavedStatus(link, save)
    }
}
*/


/*


@OptIn(ExperimentalPagingApi::class)
class TopStoriesFeedRepository @Inject constructor(
    private val dataSource: TopStoriesDataSource,
    private val feedItemDao: FeedItemDao,
    private val syncPreferences: SyncPreferences
): BaseFeedRepository<FeedItemEntity, FeedItem>(feedItemDao) {

    */
/** Paging stream for UI *//*

    fun pagingTopStories(): Flow<PagingData<FeedItem>> =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 40,
                prefetchDistance = 5,
                enablePlaceholders = false
            ),
            remoteMediator = TopStoriesRemoteMediator(
                dataSource = dataSource,
                dao = feedItemDao,
                syncPreferences = syncPreferences
            ),
            pagingSourceFactory = { feedItemDao.pagingSource() }
        )
            .flow
            .map { pagingData -> pagingData.map { it.toFeedItem() } }

    suspend fun syncTopStories(): SyncResult<FeedItem> =
        syncPreservingSaved(
            fetchRemote = {
                coroutineScope {
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
            },
            domainToEntity = { item, isSaved ->
                item.toEntity().copy(isSavedForLater = isSaved)
            },
            entityLink = { it.link },
            domainLink = { it.link },
        )

    */
/** Saved article helpers *//*

    fun isArticleSaved(link: String): Flow<Boolean> =
        dao.getItemByLink(link).map { it?.isSavedForLater == true }

    fun getSavedArticles(): Flow<List<FeedItem>> =
        dao.getSavedItems().map { list -> list.map { it.toFeedItem() } }



}

*/

/*

class TopStoriesFeedRepository @Inject constructor(
    private val dataSource: TopStoriesDataSource,
    private val feedItemDao: FeedItemDao,
    private val syncPreferences: SyncPreferences,
) : BaseFeedRepository<FeedItemEntity, FeedItem>(feedItemDao) {

    *//** Paging source for UI (Top Stories screen) *//*
    fun pagingTopStories(): Flow<PagingData<FeedItem>> =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 40,
                prefetchDistance = 5,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { feedItemDao.pagingSource() } // Room @Query invalidates on table change
        )
            .flow
            .map { pagingData -> pagingData.map { it.toFeedItem() } }

    *//** One-shot background sync (used by init / workers, NOT by pull-to-refresh) *//*
    suspend fun syncTopStories(): SyncResult<FeedItem> =
        syncPreservingSaved(
            fetchRemote = {
                coroutineScope {
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
            },
            domainToEntity = { item, isSaved ->
                item.toEntity().copy(isSavedForLater = isSaved)
            },
            entityLink = { it.link },
            domainLink = { it.link },
        )

    *//** New items since last sync (for notifications, optional) *//*
    suspend fun getNewItemsSinceLastCheck(): List<FeedItem> {
        val lastCheckTime = syncPreferences.getLastSyncTime()
        val now = System.currentTimeMillis()

        val newItems = feedItemDao
            .getAllItemsSorted() // ORDER BY timeInMil DESC in DAO
            .filter { it.timeInMil > lastCheckTime }

        syncPreferences.setLastSyncTime(now)

        return newItems.map { it.toFeedItem() }
    }

    *//** Saved state helpers *//*
    fun isArticleSaved(link: String): Flow<Boolean> =
        super.isArticleSaved(link) { it.isSavedForLater }

    fun getSavedArticles(): Flow<List<FeedItem>> =
        observeSaved { it.toFeedItem() }
}*/


/*
class TopStoriesFeedRepository @Inject constructor(
    private val dataSource: TopStoriesDataSource,
    val feedItemDao: FeedItemDao, private val syncPreferences: SyncPreferences,
) : BaseFeedRepository<FeedItemEntity, FeedItem>(feedItemDao) {

    *//** UI: observe top stories *//*
    fun getTopStoriesFeedList(): Flow<ResourceState<List<FeedItem>>> =
        observeFeed { it.toFeedItem() }


    *//** Background sync (one-shot, suspend) *//*
    suspend fun syncTopStories(): SyncResult<FeedItem> =
        syncPreservingSaved(
            fetchRemote = {
                coroutineScope {

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
            },
            domainToEntity = { item, isSaved ->
                item.toEntity().copy(isSavedForLater = isSaved)
            },
            entityLink = { it.link },
            domainLink = { it.link },


            )


    *//*suspend fun syncTopStories(): SyncResult<FeedItem> =
        syncPreservingSaved(
            fetchRemote = {
                dataSource.concatenate(
                    dataSource.getFeedList(),
                    dataSource.getGoogleTop(),
                    dataSource.getNyTop(),
                    dataSource.getNprTop(),
                    onlyRecentMillis = 172800000
                )
            },
            domainToEntity = { item, isSaved ->
                item.toEntity().copy(isSavedForLater = isSaved)
            },
            entityLink = { it.link },
            domainLink = { it.link }
        )
*//*


    suspend fun getNewItemsSinceLastCheck(): List<FeedItem> {
        val lastCheckTime = syncPreferences.getLastSyncTime()
        val now = System.currentTimeMillis()

        val allItems = feedItemDao.getAllItemsSorted()

        val newItems = allItems.filter { entity ->
            entity.timeInMil > lastCheckTime
        }

        syncPreferences.setLastSyncTime(now)

        return newItems.map { it.toFeedItem() }
    }


    *//** Observe saved state *//*
    fun isArticleSaved(link: String): Flow<Boolean> =
        super.isArticleSaved(link) { it.isSavedForLater }

    *//** Observe saved articles *//*
    fun getSavedArticles(): Flow<List<FeedItem>> =
        observeSaved { it.toFeedItem() }


    *//*fun getTopStoriesPaging(): Flow<PagingData<FeedItem>> =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { feedItemDao.pagingSource() }
        )
            .flow
            .map { pagingData -> pagingData.map { it.toFeedItem() } }
*//*

    fun pagingTopStories(): Flow<PagingData<FeedItem>> =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 40,
                prefetchDistance = 5,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { feedItemDao.pagingSource() }
        ).flow.map { pagingData ->
            pagingData.map { it.toFeedItem() }
        }




}*/
