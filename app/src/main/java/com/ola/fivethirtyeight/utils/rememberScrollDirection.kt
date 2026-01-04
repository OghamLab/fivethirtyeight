package com.ola.fivethirtyeight.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow

@Composable
fun rememberScrollDirection(listState: LazyListState): MutableState<Boolean> {
    val isScrollingUp = remember { mutableStateOf(true) }
    var lastIndex by remember { mutableIntStateOf(0) }
    var lastScrollOffset by remember { mutableIntStateOf(0) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                isScrollingUp.value = if (index != lastIndex) {
                    index < lastIndex
                } else {
                    offset < lastScrollOffset
                }
                lastIndex = index
                lastScrollOffset = offset
            }
    }
    return isScrollingUp
}
