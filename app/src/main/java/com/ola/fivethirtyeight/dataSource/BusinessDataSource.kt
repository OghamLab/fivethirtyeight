package com.ola.fivethirtyeight.dataSource

import com.ola.fivethirtyeight.model.FeedItem

interface BusinessDataSource {

    suspend fun getFeedList(): List<FeedItem>
    suspend fun getGoogleBusiness(): List<FeedItem>
    suspend fun getNyBusiness(): List<FeedItem>
    suspend fun getNprBusiness(): List<FeedItem>

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
interface BusinessDataSource {
    suspend fun concatenate(vararg lists: MutableList<FeedItem>, onlyRecentMillis: Long? = null): MutableList<FeedItem>{

         val now = System.currentTimeMillis()

         return lists
            .flatMap { it }
             .filter { item->
                 onlyRecentMillis?.let {(now - item.timeInMil) <= it }?: true

             }

            .sortedByDescending { it.timeInMil }.toMutableList()


    }


    suspend fun getFeedList(): MutableList<FeedItem>
    suspend fun getGoogleBusiness(): MutableList<FeedItem>
    suspend fun getNyBusiness(): MutableList<FeedItem>
    suspend fun getNprBusiness(): MutableList<FeedItem>
}
*/
