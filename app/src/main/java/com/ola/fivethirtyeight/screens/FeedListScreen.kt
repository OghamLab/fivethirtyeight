package com.ola.fivethirtyeight.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems

@Composable
fun <T : Any> FeedListScreen(
    pagingItems: LazyPagingItems<T>,
    savedIndex: Int,
    savedOffset: Int,
    onSaveScroll: (Int, Int) -> Unit,
    onRefresh: () -> Unit,
    cardContent: @Composable (T) -> Unit,
    shimmerContent: @Composable () -> Unit,
    emptyContent: @Composable () -> Unit,
    retryContent: @Composable (onRetry: () -> Unit) -> Unit
) {
    /* ---------- Scroll Restore ---------- */

    val listState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }

    LaunchedEffect(Unit) {
        if (savedIndex != 0 || savedOffset != 0) {
            listState.scrollToItem(savedIndex, savedOffset)
        }
    }

    LaunchedEffect(
        listState.firstVisibleItemIndex,
        listState.firstVisibleItemScrollOffset
    ) {
        onSaveScroll(
            listState.firstVisibleItemIndex,
            listState.firstVisibleItemScrollOffset
        )
    }

    // 🔑 Force refresh once when screen enters composition
    LaunchedEffect(Unit) {
        pagingItems.refresh()
    }


    /* ---------- Paging States ---------- */

    val isInitialLoading =
        pagingItems.loadState.refresh is LoadState.Loading &&
                pagingItems.itemCount == 0

    val isRefreshing =
        pagingItems.loadState.refresh is LoadState.Loading &&
                pagingItems.itemCount > 0

    val isEmpty =
        pagingItems.loadState.refresh is LoadState.NotLoading &&
                pagingItems.itemCount == 0

    val refreshError =
        pagingItems.loadState.refresh as? LoadState.Error

    val appendError =
        pagingItems.loadState.append as? LoadState.Error

    val pullRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        state = pullRefreshState,
        isRefreshing = isRefreshing,
        onRefresh = onRefresh
    ) {
        /* ---------- Initial Shimmer ---------- */

        if (isInitialLoading) {
            LazyColumn {
                items(6) { shimmerContent() }
            }
            return@PullToRefreshBox
        }

        /* ---------- Refresh Error (Full Screen) ---------- */

        if (refreshError != null && pagingItems.itemCount == 0) {
            retryContent { pagingItems.retry() }
            return@PullToRefreshBox
        }

        /* ---------- Empty State ---------- */

        if (isEmpty) {
            emptyContent()
            return@PullToRefreshBox
        }

        /* ---------- Main List ---------- */

        LazyColumn(state = listState) {

            items(
                count = pagingItems.itemCount,
                key = { pagingItems[it]?.hashCode() ?: it }
            ) { index ->
                pagingItems[index]?.let {
                    cardContent(it)
                }
            }

            /* ---------- Append Loading ---------- */

            if (pagingItems.loadState.append is LoadState.Loading) {
                item { shimmerContent() }
            }

            /* ---------- Append Error ---------- */

            appendError?.let {
                item {
                    retryContent { pagingItems.retry() }
                }
            }
        }
    }
}


/*

@Composable
fun <T : Any> FeedListScreen(
    pagingItems: LazyPagingItems<T>,
    savedIndex: Int,
    savedOffset: Int,
    onSaveScroll: (Int, Int) -> Unit,
    onRefresh: () -> Unit,
    cardContent: @Composable (T) -> Unit,
    shimmerContent: @Composable () -> Unit
) {
    val listState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }

    LaunchedEffect(Unit) {
        if (savedIndex != 0 || savedOffset != 0) {
            listState.scrollToItem(savedIndex, savedOffset)
        }
    }

    LaunchedEffect(
        listState.firstVisibleItemIndex,
        listState.firstVisibleItemScrollOffset
    ) {
        onSaveScroll(
            listState.firstVisibleItemIndex,
            listState.firstVisibleItemScrollOffset
        )
    }

    val isInitialLoading =
        pagingItems.loadState.refresh is LoadState.Loading &&
                pagingItems.itemCount == 0

    val isRefreshing =
        pagingItems.loadState.refresh is LoadState.Loading &&
                pagingItems.itemCount > 0

    val pullRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        state = pullRefreshState,
        isRefreshing = isRefreshing,
        onRefresh = onRefresh
    ) {
        if (isInitialLoading) {
            LazyColumn {
                items(6) { shimmerContent() }
            }
            return@PullToRefreshBox
        }

        LazyColumn(state = listState) {
            items(
                count = pagingItems.itemCount,
                key = { pagingItems[it]?.hashCode() ?: it }
            ) { index ->
                pagingItems[index]?.let {
                    cardContent(it)
                }
            }

            when (pagingItems.loadState.append) {
                is LoadState.Loading -> item { shimmerContent() }
                is LoadState.Error -> item { Text("Failed to load more") }
                else -> Unit
            }

            if (pagingItems.loadState.refresh is LoadState.Error) {
                item { Text("Failed to load feed") }
            }
        }
    }
}
*/


/*
@Composable
fun <T : Any> FeedListScreen(
    pagingItems: LazyPagingItems<T>,
    savedIndex: Int,
    savedOffset: Int,
    onSaveScroll: (Int, Int) -> Unit,
    onRefresh: () -> Unit,
    cardContent: @Composable (T) -> Unit,
    shimmerContent: @Composable () -> Unit
) {
    *//* ---------- Scroll Restore ---------- *//*

    val listState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }

    LaunchedEffect(Unit) {
        if (savedIndex != 0 || savedOffset != 0) {
            listState.scrollToItem(savedIndex, savedOffset)
        }
    }

    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        onSaveScroll(
            listState.firstVisibleItemIndex,
            listState.firstVisibleItemScrollOffset
        )
    }

    *//* ---------- Pull-to-Refresh ---------- *//*

    val isRefreshing = pagingItems.loadState.refresh is LoadState.Loading
    val pullRefreshState = rememberPullToRefreshState()

    *//* ---------- Cached Item Detection ---------- *//*

    val hasCachedItems = remember(pagingItems.itemCount) {
        (0 until pagingItems.itemCount).any { pagingItems.peek(it) != null }
    }

    *//* ---------- Initial Empty Handling (Optimized) ---------- *//*

    val isInitialEmpty =
        pagingItems.itemCount == 0 &&
                pagingItems.loadState.refresh is LoadState.NotLoading &&
                !hasCachedItems

    PullToRefreshBox(
        state = pullRefreshState,
        isRefreshing = isRefreshing,
        onRefresh = onRefresh
    ) {

        if (isInitialEmpty) {
            // Only shimmer if truly no cached items
            LazyColumn(state = listState) {
                items(4) { shimmerContent() }
            }
            return@PullToRefreshBox
        }

        *//* ---------- Main List ---------- *//*

        LazyColumn(state = listState) {

            items(
                count = pagingItems.itemCount,
                key = { index ->
                    pagingItems.peek(index)?.hashCode() ?: index
                }
            ) { index ->

                val item = pagingItems.peek(index) ?: pagingItems[index]

                if (item != null) {
                    cardContent(item)
                } else {
                    shimmerContent()
                }
            }

            *//* ---------- Footer: Append ---------- *//*

            when (pagingItems.loadState.append) {
                is LoadState.Loading -> item { shimmerContent() }
                is LoadState.Error -> item { Text("Failed to load more") }
                is LoadState.NotLoading -> Unit
            }

            *//* ---------- Footer: Refresh Errors ---------- *//*

            when (pagingItems.loadState.refresh) {
                is LoadState.Error -> item { Text("Failed to load feed") }
                is LoadState.Loading -> Unit
                is LoadState.NotLoading -> Unit
            }
        }
    }
}*/


/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Any> FeedListScreen(
    pagingItems: LazyPagingItems<T>,
    savedIndex: Int,
    savedOffset: Int,
    onSaveScroll: (Int, Int) -> Unit,
    onRefresh: () -> Unit,
    cardContent: @Composable (T) -> Unit,
    shimmerContent: @Composable () -> Unit
) {
    */
/* ---------- Scroll Restore ---------- *//*


    val listState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }

    LaunchedEffect(Unit) {
        if (savedIndex != 0 || savedOffset != 0) {
            listState.scrollToItem(savedIndex, savedOffset)
        }
    }

    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        onSaveScroll(
            listState.firstVisibleItemIndex,
            listState.firstVisibleItemScrollOffset
        )
    }


    */
/* ---------- Pull-to-Refresh ---------- *//*


    val isRefreshing = pagingItems.loadState.refresh is LoadState.Loading
    val pullRefreshState = rememberPullToRefreshState()

    */
/* ---------- Cached Item Detection ---------- *//*


    val hasCachedItems = remember(pagingItems.itemCount) {
        (0 until pagingItems.itemCount).any { pagingItems.peek(it) != null }
    }

    */
/* ---------- Initial Empty Handling (Optimized) ---------- *//*


    val isInitialEmpty =
        pagingItems.itemCount == 0 &&
                pagingItems.loadState.refresh is LoadState.NotLoading &&
                !hasCachedItems

    PullToRefreshBox(
        state = pullRefreshState,
        isRefreshing = isRefreshing,
        onRefresh = onRefresh
    ) {

        if (isInitialEmpty) {
            // Only shimmer if truly no cached items
            LazyColumn(state = listState) {
                items(4) { shimmerContent() }
            }
            return@PullToRefreshBox
        }

        */
/* ---------- Main List ---------- *//*


        LazyColumn(state = listState) {

            items(
                count = pagingItems.itemCount,
                key = { index ->
                    pagingItems.peek(index)?.hashCode() ?: index
                }
            ) { index ->

                val item = pagingItems.peek(index) ?: pagingItems[index]

                if (item != null) {
                    cardContent(item)
                } else {
                    shimmerContent()
                }
            }

            */
/* ---------- Footer: Append ---------- *//*


            when (pagingItems.loadState.append) {
                is LoadState.Loading -> item { shimmerContent() }
                is LoadState.Error -> item { Text("Failed to load more") }
                is LoadState.NotLoading -> Unit
            }

            */
/* ---------- Footer: Refresh Errors ---------- *//*


            when (pagingItems.loadState.refresh) {
                is LoadState.Error -> item { Text("Failed to load feed") }
                is LoadState.Loading -> Unit
                is LoadState.NotLoading -> Unit
            }
        }
    }
}
*/


/*


@Composable
fun <T : Any> FeedListScreen(
    pagingItems: LazyPagingItems<T>,
    savedIndex: Int,
    savedOffset: Int,
    onSaveScroll: (Int, Int) -> Unit,
    onRefresh: () -> Unit,
    cardContent: @Composable (T) -> Unit,
    shimmerContent: @Composable () -> Unit
) {
    */
/* ---------- Scroll Restore ---------- *//*


    val listState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }

    LaunchedEffect(Unit) {
        if (savedIndex != 0 || savedOffset != 0) {
            listState.scrollToItem(savedIndex, savedOffset)
        }
    }

    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        onSaveScroll(
            listState.firstVisibleItemIndex,
            listState.firstVisibleItemScrollOffset
        )
    }

    */
/* ---------- Pull-to-Refresh ---------- *//*


    val isRefreshing = pagingItems.loadState.refresh is LoadState.Loading
    val pullRefreshState = rememberPullToRefreshState()

    */
/* ---------- Initial Empty Handling ---------- *//*


    val isInitialEmpty =
        pagingItems.itemCount == 0 &&
                pagingItems.loadState.refresh is LoadState.NotLoading

    PullToRefreshBox(
        state = pullRefreshState,
        isRefreshing = isRefreshing,
        onRefresh = onRefresh
    ) {

        if (isInitialEmpty) {
            LazyColumn(state = listState) {
                items(6) { shimmerContent() }
            }
            return@PullToRefreshBox
        }

        */
/* ---------- Main List ---------- *//*


        LazyColumn(state = listState) {

            items(
                count = pagingItems.itemCount,
                key = { index ->
                    pagingItems.peek(index)?.hashCode() ?: index
                }
            ) { index ->

                val item = pagingItems.peek(index) ?: pagingItems[index]

                if (item != null) {
                    cardContent(item)
                } else {
                    shimmerContent()
                }
            }

            */
/* ---------- Footer: Append ---------- *//*


            when (pagingItems.loadState.append) {
                is LoadState.Loading -> item { shimmerContent() }
                is LoadState.Error -> item { Text("Failed to load more") }
                is LoadState.NotLoading -> Unit
            }

            */
/* ---------- Footer: Refresh Errors ---------- *//*


            when (pagingItems.loadState.refresh) {
                is LoadState.Error -> item { Text("Failed to load feed") }
                is LoadState.Loading -> Unit
                is LoadState.NotLoading -> Unit
            }
        }
    }
}
*/
