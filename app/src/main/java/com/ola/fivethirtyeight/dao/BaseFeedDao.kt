package com.ola.fivethirtyeight.dao

import kotlinx.coroutines.flow.Flow

interface BaseFeedDao<E> {
    fun getAllFeeds(): Flow<List<@JvmSuppressWildcards E>>
    suspend fun getAllItemsSorted(): List<@JvmSuppressWildcards E>
    suspend fun getSavedItemsOnce(): List<@JvmSuppressWildcards E>
    fun getSavedItems(): Flow<List<E>>
    fun getFeedsBySource(link: String): Flow<List<@JvmSuppressWildcards E>>

    suspend fun clearAllNonSaved()
    suspend fun upsertAll(items: List<@JvmSuppressWildcards E>)
    suspend fun updateSavedStatus(link: String, saved: Boolean)
}
