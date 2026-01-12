package com.ola.fivethirtyeight.model

data class FeedCluster(
    val main: FeedItem,          // the best version
    val variants: List<FeedItem> // other sources
)
