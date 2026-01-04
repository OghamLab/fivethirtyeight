package com.ola.fivethirtyeight.repository

import com.ola.fivethirtyeight.dao.FiveThirtyEightItemDao
import com.ola.fivethirtyeight.dataSource.NewsDataSource
import com.ola.fivethirtyeight.model.FeedItem
import com.ola.fivethirtyeight.model.FiveThirtyEightItemEntity
import com.ola.fivethirtyeight.model.toFeedItem
import com.ola.fivethirtyeight.model.toFiveThirtyEightEntity
import com.ola.fivethirtyeight.resource.ResourceState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FiveThirtyEightFeedRepository @Inject constructor(
    private val newsDataSource: NewsDataSource,
    fiveThirtyEightItemDao: FiveThirtyEightItemDao
) : BaseFeedRepository<FiveThirtyEightItemEntity, FeedItem>(fiveThirtyEightItemDao) {

    /** UI: observe politics feed */
    fun getFiveThirtyEightFeedList(): Flow<ResourceState<List<FeedItem>>> =
        observeFeed { it.toFeedItem() }

    /** Background sync (one-shot, suspend) */
    suspend fun syncFiveThirtyEight() =
        syncPreservingSaved(
            fetchRemote = {
               newsDataSource.concatenate(
                    newsDataSource.getFeedList(),
                    onlyRecentMillis = 36000800000
                )
            },
            domainToEntity = { item, isSaved ->
                item.toFiveThirtyEightEntity().copy(isSavedForLater = isSaved)
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

class FiveThirtyEightFeedRepository @Inject constructor(private val newsDataSource: NewsDataSource, private  val fiveThirtyEightItemDao: FiveThirtyEightItemDao) {

    fun getFiveThirtyEightFeedList(): Flow<ResourceState<MutableList<FeedItem>>> {
            return fiveThirtyEightItemDao.getAllFeeds()
                .map { entities ->
                    if (entities.isEmpty()) {
                        ResourceState.Loading()
                    } else {
                        ResourceState.Success(entities.map { it.toFeedItem() }.toMutableList())
                    }
                }
                .onStart { emit(ResourceState.Loading()) }
        }


        fun syncFiveThirtyEightInBackground() {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val feed = newsDataSource.getFeedList()
                    val response = newsDataSource.concatenate(
                        feed,
                        onlyRecentMillis = 36000800000
                    )

                    if (response.isNotEmpty()) {
                        fiveThirtyEightItemDao.upsertAll(response.map { it.toFiveThirtyEightEntity() })
                    }
                } catch (e: Exception) {
                    // Log error silently, fallback to cached DB content
                }
            }
        }





    suspend fun getCurrentDbFeeds(): List<FeedItem> {
        return fiveThirtyEightItemDao.getAllItemsSorted().map { it.toFeedItem() }
    }




        */
/** Toggle saved status in DB *//*

        suspend fun toggleSave(link: String, save: Boolean) {
            fiveThirtyEightItemDao.updateSavedStatus(link, save)
        }


        fun isArticleSaved(link: String): Flow<Boolean> {
            return fiveThirtyEightItemDao.getFeedsBySource(link).map { list ->
                list.firstOrNull()?.isSavedForLater == true
            }
        }


        fun getSavedArticles(): Flow<List<FeedItem>> {
            return fiveThirtyEightItemDao .getSavedItems().map { it ->
                it.map { it2 ->
                    it2.toFeedItem()
                }
            }
        }

    }












*/
