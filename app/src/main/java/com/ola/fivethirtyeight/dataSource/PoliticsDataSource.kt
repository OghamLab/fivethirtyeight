package com.ola.fivethirtyeight.dataSource

import com.ola.fivethirtyeight.model.FeedItem

interface PoliticsDataSource {
    suspend fun concatenate(vararg lists: List<FeedItem>, onlyRecentMillis: Long? = null): List<FeedItem>{
        val now = System.currentTimeMillis()

        return lists.flatMap { it }
            .filter { item->
                onlyRecentMillis?.let {(now - item.timeInMil) <= it }?: true
            }


            .sortedByDescending { it.timeInMil }.toMutableList()
    }
    suspend fun getPoliticsFeedList(): List<FeedItem>
    suspend fun getNyPolitics(): List<FeedItem>
    suspend fun getNprPolitics(): List<FeedItem>



}
