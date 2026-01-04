package com.ola.fivethirtyeight.dataSource

import com.ola.fivethirtyeight.model.FeedItem

interface SportsDataSource {
    suspend fun concatenate(vararg lists: MutableList<FeedItem>, onlyRecentMillis: Long? = null): MutableList<FeedItem>{
        val now = System.currentTimeMillis()

         return lists
            .flatMap { it}
            .filter { item ->
                onlyRecentMillis?.let {(now - item.timeInMil) <= it }?: true

            }.sortedByDescending { it.timeInMil }.toMutableList()
    }


    suspend fun getSportsFeedList(): MutableList<FeedItem>
    suspend fun getGoogleSports(): MutableList<FeedItem>
    suspend fun getNySports(): MutableList<FeedItem>
    suspend fun getNprSports(): MutableList<FeedItem>
}
