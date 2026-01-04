package com.ola.fivethirtyeight.dataSource

import com.ola.fivethirtyeight.model.FeedItem

interface TechDataSource {

    suspend fun concatenate(vararg lists: MutableList<FeedItem>, onlyRecentMillis: Long? = null): MutableList<FeedItem> {

        val now = System.currentTimeMillis()

        return lists
            .flatMap { it }
            .filter { item ->
                onlyRecentMillis?.let { (now - item.timeInMil) <= it } ?: true

            } .sortedByDescending { it.timeInMil }.toMutableList()

    }


        suspend fun getTechFeedList(): MutableList<FeedItem>
        suspend fun getGoogleTech(): MutableList<FeedItem>
        suspend fun getNyTech(): MutableList<FeedItem>
        suspend fun getNprTech(): MutableList<FeedItem>
    }
