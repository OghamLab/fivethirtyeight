package com.ola.fivethirtyeight.repository

import com.ola.fivethirtyeight.dao.WorldItemDao
import com.ola.fivethirtyeight.dataSource.WorldDataSource
import com.ola.fivethirtyeight.model.FeedItem
import com.ola.fivethirtyeight.model.WorldItemEntity
import com.ola.fivethirtyeight.model.toFeedItem
import com.ola.fivethirtyeight.model.toWorldEntity
import com.ola.fivethirtyeight.resource.ResourceState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class WorldFeedRepository @Inject constructor(
    private val worldDataSource: WorldDataSource,
    worldItemDao: WorldItemDao

) : BaseFeedRepository<WorldItemEntity, FeedItem>(worldItemDao) {

    /** UI: observe politics feed */
    fun getWorldFeedList(): Flow<ResourceState<List<FeedItem>>> =
        observeFeed { it.toFeedItem() }

    /** Background sync (one-shot, suspend) */
    suspend fun syncWorld() =
        syncPreservingSaved(
            fetchRemote = {
               worldDataSource.concatenate(
                   worldDataSource.getWorldFeedList(),
                worldDataSource.getNyWorld(),
                worldDataSource.getGoogleWorld(),
                    worldDataSource.getNprWorld(),
                    onlyRecentMillis = 172800000)
            },
            domainToEntity = { item, isSaved ->
                item.toWorldEntity().copy(isSavedForLater = isSaved)
            },
            entityLink = { it.link },
            domainLink = { it.link }
        )

    /** Observe saved state */
    fun isArticleSaved(link: String): Flow<Boolean> =
        super.isArticleSaved(link) { it.isSavedForLater }

    /** Observe saved articles */
    fun getSavedArticles(): Flow<List<FeedItem>> =
        observeSaved { it.toFeedItem() }
}

















/*
@OptIn(ExperimentalCoroutinesApi::class)
class WorldFeedRepository @Inject constructor(private val worldDataSource: WorldDataSource, private val worldItemDao: WorldItemDao) {


    fun getWorldFeedList(): Flow<ResourceState<MutableList<FeedItem>>> {
        return worldItemDao.getAllFeeds()
            .map { entities ->
                if (entities.isEmpty()) {
                    ResourceState.Loading()
                } else {
                    ResourceState.Success(entities.map { it.toFeedItem() }.toMutableList())
                }
            }
            .onStart { emit(ResourceState.Loading()) }
    }



    fun syncWorldInBackground() {
        CoroutineScope(Dispatchers.IO).launch {
            try {

                val feed = worldDataSource.getWorldFeedList()
                // val google = politicsDataSource.getGoogleTop()
                val nyTimes = worldDataSource.getNyWorld()
                val npr = worldDataSource.getNprWorld()
                val response = worldDataSource.concatenate(
                    feed, nyTimes, npr,
                    onlyRecentMillis = 172800000
                )



                if (response.isNotEmpty()) {
                    worldItemDao.upsertAll(response.map {
                        it.toWorldEntity()
                    })
                }
            } catch (e: Exception) {
                e.localizedMessage
            }
        }
    }



    suspend fun getCurrentDbFeeds(): List<FeedItem> {
        return worldItemDao.getAllItemsSorted().map { it.toFeedItem() }
    }
}


*/
