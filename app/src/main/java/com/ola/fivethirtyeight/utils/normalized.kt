package com.ola.fivethirtyeight.utils

import com.ola.fivethirtyeight.model.FeedItem

fun FeedItem.normalized(): FeedItem = copy(
    imageUrl = imageUrl
        .takeIf { it.isNotBlank() && !it.contains("default") && !it.contains("null") }
        .orEmpty(),
    description = description.trim()
)
