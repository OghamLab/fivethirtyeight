package com.ola.fivethirtyeight.dataSource

import com.ola.fivethirtyeight.model.FeedItem

interface HealthDataSource {
    suspend fun concatenate(vararg lists: List<FeedItem>, onlyRecentMillis: Long? = null): List<FeedItem>{

         val now = System.currentTimeMillis()

         return lists
            .flatMap { it }
            .filter { item ->
                onlyRecentMillis?.let {(now - item.timeInMil) <= it }?: true

            }.sortedByDescending { it.timeInMil }.toList()
    }


    suspend fun getHealthFeedList(): List<FeedItem>
    suspend fun getGoogleHealth(): List<FeedItem>
    suspend fun getGoogleScience(): List<FeedItem>
    suspend fun getNyHealth(): List<FeedItem>
    suspend fun getNprHealth(): List<FeedItem>
}
