package com.ola.fivethirtyeight.repository


import com.ola.fivethirtyeight.dao.BusinessItemDao
import com.ola.fivethirtyeight.dataSource.BusinessDataSource
import com.ola.fivethirtyeight.datastore.SyncPreferences
import com.ola.fivethirtyeight.model.BusinessItemEntity
import com.ola.fivethirtyeight.model.FeedItem
import com.ola.fivethirtyeight.model.toBusinessEntity
import com.ola.fivethirtyeight.model.toFeedItem
import com.ola.fivethirtyeight.resource.ResourceState
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BusinessFeedRepository
@Inject constructor(private val businessDataSource: BusinessDataSource,
     businessItemDao: BusinessItemDao, private val syncPreferences: SyncPreferences, ): BaseFeedRepository<BusinessItemEntity, FeedItem>(businessItemDao)
{

    /** UI: observe politics feed */
    fun getBusinessFeedList(): Flow<ResourceState<List<FeedItem>>> =
        observeFeed { it.toFeedItem() }

    /** Background sync (one-shot, suspend) */
    suspend fun syncBusiness() =
        syncPreservingSaved(
            fetchRemote = {
                coroutineScope {
                    val abc = async {businessDataSource.getFeedList()}
                    val google = async {businessDataSource.getGoogleBusiness()}
                    val ny = async {businessDataSource.getNyBusiness()}
                    val npr = async {businessDataSource.getNprBusiness()}

                    businessDataSource.concatenate(
                        abc.await(),
                        google.await(),
                        ny.await(),
                        npr.await(),
                        onlyRecentMillis = 172800000



                    )
                }
            },
            domainToEntity = { item, isSaved ->
                item.toBusinessEntity().copy(isSavedForLater = isSaved)
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











    /*fun getBusinessFeedList(): Flow<ResourceState<MutableList<FeedItem>>> {
        return businessItemDao.getAllFeeds()
            .map { entities ->
                if (entities.isEmpty()) {
                    ResourceState.Loading()
                } else {
                    ResourceState.Success(entities.map { it.toFeedItem() }.toMutableList())
                }
            }
            .onStart { emit(ResourceState.Loading()) }
    }

    fun syncBusinessInBackground() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val feed = businessDataSource.getFeedList()
                val google = businessDataSource.getGoogleBusiness()
                val nyTimes = businessDataSource.getNyBusiness()
                val npr = businessDataSource.getNprBusiness()


                val response =businessDataSource.concatenate(
                    feed, google, nyTimes, npr,
                    onlyRecentMillis = 172800000
                )

                if (response.isNotEmpty()) {
                    businessItemDao.upsertAll(response.map { it.toBusinessEntity() })
                }
            } catch (e: Exception) {
                // Log error silently, fallback to cached DB content
            }
        }

    }






    suspend fun getCurrentDbFeeds(): List<FeedItem> {
        return businessItemDao.getAllItemsSorted().map { it.toFeedItem() }
    }




    *//** Toggle saved status in DB *//*
    suspend fun toggleSave(link: String, save: Boolean) {
        businessItemDao.updateSavedStatus(link, save)
    }


    fun isArticleSaved(link: String): Flow<Boolean> {
        return businessItemDao.getFeedsBySource(link).map { list ->
            list.firstOrNull()?.isSavedForLater == true
        }
    }


    fun getSavedArticles(): Flow<List<FeedItem>> {
        return businessItemDao.getSavedItems().map {it->
            it.map {it2 ->
                it2.toFeedItem()
            }
        }
    }


*/

}







