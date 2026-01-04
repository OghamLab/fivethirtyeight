package com.ola.fivethirtyeight.repository
import com.ola.fivethirtyeight.dao.PoliticsItemDao
import com.ola.fivethirtyeight.dataSource.PoliticsDataSource
import com.ola.fivethirtyeight.model.FeedItem
import com.ola.fivethirtyeight.model.PoliticsItemEntity
import com.ola.fivethirtyeight.model.toFeedItem
import com.ola.fivethirtyeight.model.toPolEntity
import com.ola.fivethirtyeight.resource.ResourceState
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PoliticsFeedRepository @Inject constructor(
    private val politicsDataSource: PoliticsDataSource,
    politicsItemDao: PoliticsItemDao
) : BaseFeedRepository<PoliticsItemEntity, FeedItem>(politicsItemDao) {

    /** UI: observe politics feed */
    fun getPoliticsFeedList(): Flow<ResourceState<List<FeedItem>>> =
        observeFeed { it.toFeedItem() }

    /** Background sync (one-shot, suspend) */
    suspend fun syncPolitics() =
        syncPreservingSaved(
            fetchRemote = {
                coroutineScope {

            val abc=  async{politicsDataSource.getPoliticsFeedList()}
                    val ny = async {politicsDataSource.getNyPolitics()}
                    val npr = async    {politicsDataSource.getNprPolitics()}

                    politicsDataSource.concatenate(
                        abc.await(),
                        ny.await(),
                       npr.await(),
                       onlyRecentMillis = 172800000


                    )
                }

            },
            domainToEntity = { item, isSaved ->
                item.toPolEntity().copy(isSavedForLater = isSaved)
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
