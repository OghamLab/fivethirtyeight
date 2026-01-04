package com.ola.fivethirtyeight.repository

import com.ola.fivethirtyeight.dao.BaseFeedDao
import com.ola.fivethirtyeight.model.SyncResult
import com.ola.fivethirtyeight.resource.ResourceState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

abstract class BaseFeedRepository<Entity, Domain>(
    protected val dao: BaseFeedDao<Entity>
) {

    protected fun observeFeed(
        entityToDomain: (Entity) -> Domain
    ): Flow<ResourceState<List<Domain>>> =
        dao.getAllFeeds()
            .map { entities ->
                if (entities.isEmpty()) {
                    ResourceState.Loading()
                } else {
                    ResourceState.Success(
                        entities.map(entityToDomain)
                    )
                }
            }
            .onStart { emit(ResourceState.Loading()) }


    protected suspend fun syncPreservingSaved(
        fetchRemote: suspend () -> List<Domain>,
        domainToEntity: (Domain, Boolean) -> Entity,
        entityLink: (Entity) -> String,
        domainLink: (Domain) -> String
    ): SyncResult<Domain> {

        val remote = fetchRemote()
        if (remote.isEmpty()) {
            return SyncResult(emptyList(), 0)
        }

        // Snapshot BEFORE mutation
        val existingLinks = dao.getAllItemsSorted()
            .map(entityLink)
            .toSet()

        val savedLinks = dao.getSavedItemsOnce()
            .map(entityLink)
            .toSet()

        // 🔥 Detect new items BEFORE delete/insert
        val newItems = remote.filterNot { domainLink(it) in existingLinks }

        // Preserve saved state
        dao.clearAllNonSaved()

        dao.upsertAll(
            remote.map { item ->
                domainToEntity(item, domainLink(item) in savedLinks)
            }
        )

        return SyncResult(
            newItems = newItems,
            fetchedCount = remote.size
        )
    }


    suspend fun toggleSave(link: String, save: Boolean) =
        dao.updateSavedStatus(link, save)

    fun isArticleSaved(
        link: String,
        entitySaved: (Entity) -> Boolean
    ): Flow<Boolean> =
        dao.getFeedsBySource(link)
            .map { it.firstOrNull()?.let(entitySaved) == true }

    fun observeSaved(
        entityToDomain: (Entity) -> Domain
    ): Flow<List<Domain>> =
        dao.getSavedItems().map { it.map(entityToDomain) }

    suspend fun snapshot(
        entityToDomain: (Entity) -> Domain
    ): List<Domain> =
        dao.getAllItemsSorted().map(entityToDomain)
}
