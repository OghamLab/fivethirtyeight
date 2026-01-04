package com.ola.fivethirtyeight.notification

import com.ola.fivethirtyeight.model.FeedItem


enum class FeedPriority(
    val breakingWindowMs: Long,
    val alwaysNotify: Boolean
) {
    TOP_STORIES(
        breakingWindowMs = 7 * 60 * 1000L,   // 7 minutes
        alwaysNotify = true                  // Top Stories = high authority
    )
}


 fun isBreaking(item: FeedItem, now: Long): Boolean {
    val published = item.timeInMil ?: return false
    val priority = FeedPriority.TOP_STORIES
    return now - published <= priority.breakingWindowMs
}
