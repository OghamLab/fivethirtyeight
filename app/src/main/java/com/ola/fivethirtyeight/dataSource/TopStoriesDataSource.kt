package com.ola.fivethirtyeight.dataSource

import com.ola.fivethirtyeight.model.FeedItem


interface TopStoriesDataSource {

    /** Fetch all configured feeds */
    suspend fun fetchAllFeeds(): List<FeedItem>

    /** Optional: fetch a single feed by name */
    suspend fun fetchFeed(name: String): List<FeedItem>

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

*/






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
