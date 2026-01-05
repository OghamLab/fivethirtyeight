package com.ola.fivethirtyeight.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feed_refresh")
data class FeedRefreshEntity(
    @PrimaryKey val id: Int = 0, // single-row table
    val lastRefreshTime: Long
)
