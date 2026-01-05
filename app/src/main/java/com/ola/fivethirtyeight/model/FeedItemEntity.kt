package com.ola.fivethirtyeight.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "feed_items",
    indices = [
        Index("timeInMil"),
        Index("isSavedForLater"),
        Index("publishedAt"),

    ]
)
data class FeedItemEntity(
    val title: String,
    val description: String,
    val content: String,
    val author: String,
    val publishedAt: String,
    val imageUrl: String,

    @PrimaryKey
    val link: String,

    val savedDate: String,
    val timeInMil: Long,
    val isSavedForLater: Boolean
)


/*
@Entity(
    tableName = "feed_items",
    indices = [
        Index("timeInMil"),
        Index("link", unique = true)
    ]
)

data class FeedItemEntity(
    val title: String, val description: String, val content: String, val author: String, val publishedAt: String, val imageUrl: String, @PrimaryKey val link: String,
    val savedDate: String, val timeInMil: Long, val isSavedForLater: Boolean = false,

    
    )
*/


