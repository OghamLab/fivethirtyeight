package com.ola.fivethirtyeight.utils

import com.ola.fivethirtyeight.model.FeedItem

/*fun hasValidImage(item: FeedItem): Boolean =
    !item.imageUrl.isNullOrBlank() &&
            !item.imageUrl.contains("null", true) &&
            !item.imageUrl.contains("default", true)*/



fun List<FeedItem>.withHeroOnTop(): List<FeedItem> {
    val hero = firstOrNull { !it.imageUrl.isNullOrBlank() }
    return if (hero == null) this else listOf(hero) + filter { it != hero }
}
