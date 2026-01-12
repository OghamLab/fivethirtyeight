package com.ola.fivethirtyeight.model


data class FeedItem(
    val title: String, val description: String, val content: String, val author: String, val publishedAt: String, val imageUrl: String,  val link: String,
    val savedDate: String, val timeInMil: Long, val isSavedForLater: Boolean = false,

){



}

