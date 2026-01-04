package com.ola.fivethirtyeight.dataSource

import com.ola.fivethirtyeight.model.FeedItem

interface WorldDataSource {



    suspend fun concatenate(vararg lists: MutableList<FeedItem>, onlyRecentMillis: Long? = null): MutableList<FeedItem>{
        val now = System.currentTimeMillis()

        return lists.flatMap { it }
            .filter { item ->
                onlyRecentMillis?.let { (now - item.timeInMil) <= it } ?: true
            }

            .sortedByDescending { it.timeInMil }.toMutableList()
    }


    suspend fun getWorldFeedList(): MutableList<FeedItem>
    suspend fun getGoogleWorld(): MutableList<FeedItem>
    suspend fun getNyWorld(): MutableList<FeedItem>
    suspend fun getNprWorld(): MutableList<FeedItem>



}
