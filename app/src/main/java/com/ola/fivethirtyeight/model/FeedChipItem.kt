package com.ola.fivethirtyeight.model

import androidx.compose.runtime.Composable

data class FeedChipItem(
    val label: String,
    val screen: @Composable () -> Unit
)
