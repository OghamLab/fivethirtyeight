package com.ola.fivethirtyeight.model

import androidx.room.PrimaryKey


data class FeedItem(
    val title: String, val description: String, val content: String, val author: String, val publishedAt: String, val imageUrl: String, @PrimaryKey val link: String,
    val savedDate: String, val timeInMil: Long, val isSavedForLater: Boolean = false,

){



}

