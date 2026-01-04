package com.ola.fivethirtyeight.tabs

import androidx.compose.runtime.Composable

data class ImageTabItem(
    val text: String,//Tab Title
    val screen: @Composable ()->Unit//Tab Screen(can also take params)
)