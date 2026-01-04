package com.ola.fivethirtyeight.repository

import com.ola.fivethirtyeight.dao.HealthItemDao
import com.ola.fivethirtyeight.dataSource.HealthDataSource
import com.ola.fivethirtyeight.model.FeedItem
import com.ola.fivethirtyeight.model.HealthItemEntity
import com.ola.fivethirtyeight.model.toFeedItem
import com.ola.fivethirtyeight.model.toHealthEntity
import com.ola.fivethirtyeight.resource.ResourceState
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class HealthFeedRepository @Inject constructor(
    private val healthDataSource: HealthDataSource,
    healthDao: HealthItemDao
) : BaseFeedRepository<HealthItemEntity, FeedItem>(healthDao) {

    /** UI: observe politics feed */
    fun getHealthFeedList(): Flow<ResourceState<List<FeedItem>>> =
        observeFeed { it.toFeedItem() }

    /** Background sync (one-shot, suspend) */
    suspend fun syncHealth() =
        syncPreservingSaved(
            fetchRemote = {

                coroutineScope {
                    val abc = async     {healthDataSource.getHealthFeedList()}
                val googleHealth = async    {healthDataSource.getGoogleHealth()}
                val googleScience = async  {healthDataSource.getGoogleScience()}
                val ny = async {healthDataSource.getNyHealth()}
                val npr = async  {healthDataSource.getNprHealth()}

                  healthDataSource.concatenate(
                    abc.await(),
                    googleHealth.await(),
                    googleScience.await(),
                    ny.await(),
                    npr.await(),
                    onlyRecentMillis = 172800000

                  )
                }
            },

            domainToEntity = { item, isSaved ->
                item.toHealthEntity().copy(isSavedForLater = isSaved)
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
class HealthFeedRepository @Inject constructor(private val healthDataSource: HealthDataSource, private val healthItemDao: HealthItemDao
) {

    fun getHealthFeedList(): Flow<ResourceState<MutableList<FeedItem>>> {
        return healthItemDao.getAllFeeds()
            .map { entities ->
                if (entities.isEmpty()) {
                    ResourceState.Loading()
                } else {
                    ResourceState.Success(entities.map { it.toFeedItem() }.toMutableList())
                }
            }
            .onStart { emit(ResourceState.Loading()) }
    }


    fun syncHealthInBackground() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val feed = healthDataSource.getHealthFeedList()
                val google = healthDataSource.getGoogleHealth()
                val googleScience = healthDataSource.getGoogleScience()
                val nyTimes = healthDataSource.getNyHealth()
                val npr = healthDataSource.getNprHealth()

                val response = healthDataSource.concatenate(
                    feed,
                    google,
                    nyTimes,
                    npr,
                    googleScience,
                    onlyRecentMillis = 172800000
                )

                if (response.isNotEmpty()) {
                    healthItemDao.upsertAll(response.map { it.toHealthEntity() })
                }
            } catch (e: Exception) {
               e.localizedMessage
            }
        }
    }








    suspend fun getCurrentDbFeeds(): List<FeedItem> {
        return healthItemDao.getAllItemsSorted().map { it.toFeedItem() }
    }


    */
/** Toggle saved status in DB *//*

        suspend fun toggleSave(link: String, save: Boolean) {
            healthItemDao.updateSavedStatus(link, save)
        }


        fun isArticleSaved(link: String): Flow<Boolean> {
            return healthItemDao.getFeedsBySource(link).map { list ->
                list.firstOrNull()?.isSavedForLater == true
            }
        }


        fun getSavedArticles(): Flow<List<FeedItem>> {
            return healthItemDao.getSavedItems().map {it->
                it.map {it2 ->
                    it2.toFeedItem()
                }
            }
        }
}

*/







