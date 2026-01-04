package com.ola.fivethirtyeight.utils

import androidx.compose.runtime.Composable
import com.ola.fivethirtyeight.model.FeedItem
import com.ola.fivethirtyeight.screens.FeedCardDispatcher
import com.ola.fivethirtyeight.screens.FeedCardDispatcherSaved

@Composable
fun AnimatedFeedCard(
    item: FeedItem,
    onClick: (FeedItem) -> Unit
) {
    FeedCardDispatcher(item = item, onArticleClick = {
        onClick(it)
    })
}



@Composable
fun AnimatedFeedCardSaved(
    item: FeedItem,
    onClick: (FeedItem) -> Unit
) {
    FeedCardDispatcherSaved(item = item, onArticleClick = {
        onClick(it)
    })
}

