package com.ola.fivethirtyeight.uimodel

import com.ola.fivethirtyeight.model.FeedItem

sealed class TopStoriesUiModel {
    data class Story(val item: FeedItem) : TopStoriesUiModel()
    data class Promoted(val id: String) : TopStoriesUiModel()
}
