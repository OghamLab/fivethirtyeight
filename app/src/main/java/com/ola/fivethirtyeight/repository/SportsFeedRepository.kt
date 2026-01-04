package com.ola.fivethirtyeight.repository

import com.ola.fivethirtyeight.dao.SportsItemDao
import com.ola.fivethirtyeight.dataSource.SportsDataSource
import com.ola.fivethirtyeight.model.FeedItem
import com.ola.fivethirtyeight.model.SportsItemEntity
import com.ola.fivethirtyeight.model.toFeedItem
import com.ola.fivethirtyeight.model.toSportsEntity
import com.ola.fivethirtyeight.resource.ResourceState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class SportsFeedRepository @Inject constructor(
    private val sportsDataSource: SportsDataSource,

    sportsItemDao: SportsItemDao
) : BaseFeedRepository<SportsItemEntity, FeedItem>(sportsItemDao) {

    /** UI: observe politics feed */
    fun getSportsFeedList(): Flow<ResourceState<List<FeedItem>>> =
        observeFeed { it.toFeedItem() }

    /** Background sync (one-shot, suspend) */
    suspend fun syncSports() =
        syncPreservingSaved(
            fetchRemote = {
               sportsDataSource.concatenate(
                 sportsDataSource.getSportsFeedList(),
               sportsDataSource.getGoogleSports(),
                sportsDataSource.getNySports(),
                sportsDataSource.getNprSports(),
                    onlyRecentMillis = 172800000)
            },
            domainToEntity = { item, isSaved ->
                item.toSportsEntity().copy(isSavedForLater = isSaved)
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
class SportsFeedRepository @Inject constructor(private val sportsDataSource: SportsDataSource, private val sportsItemDao: SportsItemDao
) {

    fun getSportsFeedList(): Flow<ResourceState<MutableList<FeedItem>>> {
        return sportsItemDao.getAllFeeds()
            .map { entities ->
                if (entities.isEmpty()) {
                    ResourceState.Loading()
                } else {
                    ResourceState.Success(entities.map { it.toFeedItem() }.toMutableList())
                }
            }
            .onStart { emit(ResourceState.Loading()) }
    }


    fun syncSportsInBackground() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val feed = sportsDataSource.getSportsFeedList()
                val google = sportsDataSource.getGoogleSports()
                val nyTimes = sportsDataSource.getNySports()
                val npr = sportsDataSource.getNprSports()

                val response = sportsDataSource.concatenate(
                    feed,
                    google,
                    nyTimes,
                    npr,
                    onlyRecentMillis = 172800000
                )

                if (response.isNotEmpty()) {
                    sportsItemDao.upsertAll(response.map { it.toSportsEntity() })
                }
            } catch (e: Exception) {
                // Log error silently, fallback to cached DB content
            }
        }
    }


    suspend fun getCurrentDbFeeds(): List<FeedItem> {
        return sportsItemDao.getAllItemsSorted().map { it.toFeedItem() }
    }


        */
/** Toggle saved status in DB *//*

        suspend fun toggleSave(link: String, save: Boolean) {
            sportsItemDao.updateSavedStatus(link, save)
        }


        fun isArticleSaved(link: String): Flow<Boolean> {
            return sportsItemDao.getFeedsBySource(link).map { list ->
                list.firstOrNull()?.isSavedForLater == true
            }
        }


        fun getSavedArticles(): Flow<List<FeedItem>> {
            return sportsItemDao.getSavedItems().map { it ->
                it.map { it2 ->
                    it2.toFeedItem()
                }
            }
        }

    }















*/






