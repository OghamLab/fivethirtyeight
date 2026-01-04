package com.ola.fivethirtyeight.dataSource

import com.ola.fivethirtyeight.model.FeedItem


interface TopStoriesDataSource {

    suspend fun getFeedList(): List<FeedItem>
    suspend fun getGoogleTop(): List<FeedItem>
    suspend fun getNyTop(): List<FeedItem>
    suspend fun getNprTop(): List<FeedItem>

    suspend fun concatenate(
        vararg lists: List<FeedItem>,
        onlyRecentMillis: Long? = null
    ): List<FeedItem> {
        val now = System.currentTimeMillis()

        return lists
            .asSequence()
            .flatten()
            .filter { item ->
                onlyRecentMillis?.let { (now - item.timeInMil) <= it } ?: true
            }
            .sortedByDescending { it.timeInMil }
            .toList()
    }
}







/*
interface TopStoriesDataSource {
    suspend fun concatenate(vararg lists: List<FeedItem>, onlyRecentMillis: Long? = null): List<FeedItem>{

         val now = System.currentTimeMillis()

         return lists
            .flatMap { it }
            .filter {item ->
                onlyRecentMillis?.let {(now - item.timeInMil) <= it }?: true

            }.sortedByDescending { it.timeInMil }.toList()
    }


    suspend fun getFeedList(): List<FeedItem>
    suspend fun getGoogleTop(): List<FeedItem>
    suspend fun getNyTop(): List<FeedItem>
    suspend fun getNprTop(): List<FeedItem>
}
*/
