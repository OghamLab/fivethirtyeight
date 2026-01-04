package com.ola.fivethirtyeight.dataSource

import com.ola.fivethirtyeight.model.FeedItem

interface NewsDataSource {


    suspend fun concatenate(vararg lists: MutableList<FeedItem>, onlyRecentMillis: Long? = null): MutableList<FeedItem>{

        val now = System.currentTimeMillis()

        return lists
            .flatMap { it }
            .filter {item ->
                onlyRecentMillis?.let {(now - item.timeInMil) <= it }?: true

            }.sortedByDescending { it.timeInMil }.toMutableList()
    }

    suspend fun getFeedList(): MutableList<FeedItem>

}







