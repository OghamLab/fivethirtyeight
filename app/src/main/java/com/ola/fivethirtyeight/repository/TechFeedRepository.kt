package com.ola.fivethirtyeight.repository

import com.ola.fivethirtyeight.dao.TechItemDao
import com.ola.fivethirtyeight.dataSource.TechDataSource
import com.ola.fivethirtyeight.model.FeedItem
import com.ola.fivethirtyeight.model.TechItemEntity
import com.ola.fivethirtyeight.model.toFeedItem
import com.ola.fivethirtyeight.model.toTechEntity
import com.ola.fivethirtyeight.resource.ResourceState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class TechFeedRepository @Inject constructor(
    private val techDataSource: TechDataSource,
    techItemDao: TechItemDao
) : BaseFeedRepository<TechItemEntity, FeedItem>(techItemDao) {

    /** UI: observe politics feed */
    fun getTechFeedList(): Flow<ResourceState<List<FeedItem>>> =
        observeFeed { it.toFeedItem() }

    /** Background sync (one-shot, suspend) */
    suspend fun syncTech() =
        syncPreservingSaved(
            fetchRemote = {
                techDataSource.concatenate(
              techDataSource.getTechFeedList(),
                 techDataSource.getGoogleTech(),
             techDataSource.getNyTech(),
               techDataSource.getNprTech(),
                    onlyRecentMillis = 172800000
                )
            },
            domainToEntity = { item, isSaved ->
                item.toTechEntity().copy(isSavedForLater = isSaved)
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

class TechFeedRepository @Inject constructor(private val techDataSource: TechDataSource, private val techItemDao: TechItemDao
) {

    fun getTechFeedList(): Flow<ResourceState<MutableList<FeedItem>>> {
        return techItemDao.getAllFeeds()
            .map { entities ->
                if (entities.isEmpty()) {
                    ResourceState.Loading()
                } else {
                    ResourceState.Success(entities.map { it.toFeedItem() }.toMutableList())
                }
            }
            .onStart { emit(ResourceState.Loading()) }
    }



    fun syncTechInBackground() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val feed = techDataSource.getTechFeedList()
                val google = techDataSource.getGoogleTech()
                val nyTimes = techDataSource.getNyTech()
                val npr = techDataSource.getNprTech()

                val response =techDataSource.concatenate(
                    feed, google, nyTimes, npr,
                    onlyRecentMillis = 172800000
                )

                if (response.isNotEmpty()) {
                    techItemDao.upsertAll(response.map { it.toTechEntity() })
                }
            } catch (e: Exception) {
                // Log error silently, fallback to cached DB content
            }
        }

    }

    suspend fun getCurrentDbFeeds(): List<FeedItem> {
        return techItemDao.getAllItemsSorted().map { it.toFeedItem() }
    }



    */
/** Toggle saved status in DB *//*

    suspend fun toggleSave(link: String, save: Boolean) {
        techItemDao.updateSavedStatus(link, save)
    }


    fun isArticleSaved(link: String): Flow<Boolean> {
        return techItemDao.getFeedsBySource(link).map { list ->
            list.firstOrNull()?.isSavedForLater == true
        }
    }


    fun getSavedArticles(): Flow<List<FeedItem>> {
        return techItemDao.getSavedItems().map {it->
            it.map {it2 ->
                it2.toFeedItem()
            }
        }
    }

}











*/
