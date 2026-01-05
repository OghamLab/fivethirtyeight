package com.ola.fivethirtyeight.screens

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.ola.fivethirtyeight.model.FeedItem
import com.ola.fivethirtyeight.utils.AnimatedFeedCard
import com.ola.fivethirtyeight.utils.ShimmerFeedCard
import com.ola.fivethirtyeight.viewmodel.SharedViewModel


@Composable
fun TopStoriesScreen(
    viewModel: SharedViewModel = hiltViewModel(),
    onArticleClick: (FeedItem) -> Unit
) {




    val pagingItems = viewModel.topStoriesPaging.collectAsLazyPagingItems()






    val scroll = viewModel.scrollStateFor(SharedViewModel.FeedTab.TOP)
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = scroll.index,
        initialFirstVisibleItemScrollOffset = scroll.offset
    )

    FeedListScreen(
        pagingItems = pagingItems,
        listState= listState,
        savedIndex = scroll.index,
        savedOffset = scroll.offset,
        onSaveScroll = { index, offset ->
            viewModel.saveScrollPosition(SharedViewModel.FeedTab.TOP, index, offset)
        },
        onRefresh =
            { pagingItems.refresh() },
        cardContent = { item ->
            AnimatedFeedCard(item) { onArticleClick(item) }
        },
        shimmerContent = { ShimmerFeedCard() }
    )
}






/*
@Composable
fun TopStoriesScreen(
    viewModel: SharedViewModel = hiltViewModel(),
    onArticleClick: (FeedItem) -> Unit
) {
    val pagingItems = viewModel.topStoriesPaging.collectAsLazyPagingItems()
    val scrollState = viewModel.scrollStates.collectAsState().value
    val topScroll = scrollState[SharedViewModel.FeedTab.TOP]
        ?: SharedViewModel.ScrollStateSnapshot()

    FeedListScreen(
        pagingItems = pagingItems,
        savedIndex = topScroll.index,
        savedOffset = topScroll.offset,
        onSaveScroll = { index, offset ->
            viewModel.saveScrollPosition(SharedViewModel.FeedTab.TOP, index, offset)
        },
        onRefresh = { pagingItems.refresh() },
        cardContent = { item ->
            AnimatedFeedCard(item) { onArticleClick(item) }
        },
        shimmerContent = { ShimmerFeedCard() }
    )
}

*/

/*

@Composable
fun TopStoriesScreen(
    viewModel: SharedViewModel = hiltViewModel(),
    onArticleClick: (FeedItem) -> Unit
) {
    val pagingItems = viewModel.topStoriesPaging.collectAsLazyPagingItems()

    FeedListScreen(
        pagingItems = pagingItems,
        savedIndex = viewModel.firstVisibleItemIndex.collectAsState().value,
        savedOffset = viewModel.firstVisibleItemScrollOffset.collectAsState().value,
        onSaveScroll = viewModel::saveScrollPosition,
        onRefresh = { pagingItems.refresh() },
        cardContent = { item ->
            AnimatedFeedCard(item) { onArticleClick(item) }
        },
        shimmerContent = { ShimmerFeedCard() }
    )
}
*/


/*

@Composable
fun TopStoriesScreen(
    viewModel: SharedViewModel = hiltViewModel(),
    onArticleClick: (FeedItem) -> Unit
) {
    val pagingItems = viewModel.topStoriesPaging.collectAsLazyPagingItems()

    */
/*//*
/ 🔑 REQUIRED: force refresh after process restart
    LaunchedEffect(Unit) {
        pagingItems.refresh()
    }*//*


    FeedListScreen(
        pagingItems = pagingItems,
        savedIndex = viewModel.firstVisibleItemIndex.collectAsState().value,
        savedOffset = viewModel.firstVisibleItemScrollOffset.collectAsState().value,
        onSaveScroll = viewModel::saveScrollPosition,
        onRefresh = { pagingItems.refresh() },
        cardContent = { AnimatedFeedCard(it) { onArticleClick(it) } },
        shimmerContent = { ShimmerFeedCard() },
       // emptyContent = { FeedEmptyState() },
       // retryContent = { FeedRetryState(it) }
    )
}
*/








/*@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopStoriesScreen(
    viewModel: SharedViewModel = hiltViewModel(),
    onArticleClick: (FeedItem) -> Unit
) {
    val pagingItems = viewModel.topStoriesPaging.collectAsLazyPagingItems()

    FeedListScreen(
        pagingItems = pagingItems,
        savedIndex = viewModel.firstVisibleItemIndex.collectAsState().value,
        savedOffset = viewModel.firstVisibleItemScrollOffset.collectAsState().value,
        onSaveScroll = viewModel::saveScrollPosition,
        onRefresh = { pagingItems.refresh() },
        cardContent = { AnimatedFeedCard(it) { onArticleClick(it) } },
        shimmerContent = { ShimmerFeedCard() },
        emptyContent = { FeedEmptyState() },
        retryContent = { onRetry -> FeedRetryState(onRetry) }
    )
}*/


/*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopStoriesScreen(
    viewModel: SharedViewModel = hiltViewModel(),
    onArticleClick: (FeedItem) -> Unit
) {
    val pagingItems = viewModel.topStoriesPaging.collectAsLazyPagingItems()

    FeedListScreen(
        pagingItems = pagingItems,
        savedIndex = viewModel.firstVisibleItemIndex.collectAsState().value,
        savedOffset = viewModel.firstVisibleItemScrollOffset.collectAsState().value,
        onSaveScroll = viewModel::saveScrollPosition,
        onRefresh = { pagingItems.refresh() },
        cardContent = { item ->
            AnimatedFeedCard(item) { onArticleClick(item) }
        },
        shimmerContent = { ShimmerFeedCard() }
    )
}
*/


/*
@Composable
fun TopStoriesScreen(
    viewModel: SharedViewModel = hiltViewModel(),
    onArticleClick: (FeedItem) -> Unit
) {
    val pagingItems = viewModel.topStoriesPaging.collectAsLazyPagingItems()

    FeedListScreen(
        pagingItems = pagingItems,
        savedIndex = viewModel.firstVisibleItemIndex.collectAsState().value,
        savedOffset = viewModel.firstVisibleItemScrollOffset.collectAsState().value,
        onSaveScroll = viewModel::saveScrollPosition,
        onRefresh = { pagingItems.refresh() },
        cardContent = { item ->
            AnimatedFeedCard(item) { onArticleClick(item) }
        },
        shimmerContent = { ShimmerFeedCard() }
    )
}*/


/*
@Composable
fun TopStoriesScreen(
    viewModel: SharedViewModel = hiltViewModel(),
    onArticleClick: (FeedItem) -> Unit
) {
    val pagingItems = viewModel.topStoriesPaging.collectAsLazyPagingItems()

    */
/* ---------- Scroll Restore ---------- *//*


    val savedIndex by viewModel.firstVisibleItemIndex.collectAsState()
    val savedOffset by viewModel.firstVisibleItemScrollOffset.collectAsState()

    val listState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }

    // Restore scroll position when returning to this screen
    LaunchedEffect(Unit) {
        if (savedIndex != 0 || savedOffset != 0) {
            listState.scrollToItem(savedIndex, savedOffset)
        }
    }

    // Save scroll position as user scrolls
    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        viewModel.saveScrollPosition(
            listState.firstVisibleItemIndex,
            listState.firstVisibleItemScrollOffset
        )
    }

    */
/* ---------- Pull-to-Refresh ---------- *//*


    val isRefreshing = pagingItems.loadState.refresh is LoadState.Loading
    val pullRefreshState = rememberPullToRefreshState()

    */
/* ---------- Initial Empty Handling (Corrected) ---------- *//*


    val isInitialEmpty =
        pagingItems.itemCount == 0 &&
                pagingItems.loadState.refresh is LoadState.NotLoading

    PullToRefreshBox(
        state = pullRefreshState,
        isRefreshing = isRefreshing,
        onRefresh = { pagingItems.refresh() }
    ) {

        if (isInitialEmpty) {
            // Avoid blank screen on first emission or back navigation
            LazyColumn(state = listState) {
                items(6) { ShimmerFeedCard() }
            }
            return@PullToRefreshBox
        }

        */
/* ---------- Main List ---------- *//*


        LazyColumn(state = listState) {

            items(
                count = pagingItems.itemCount,
                key = { index ->
                    // Stable key: prefer cached item
                    pagingItems.peek(index)?.link ?: index
                }
            ) { index ->

                // Instant rendering when returning to screen
                val item = pagingItems.peek(index) ?: pagingItems[index]

                if (item != null) {
                    AnimatedFeedCard(item) {
                        onArticleClick(item)
                    }
                } else {
                    ShimmerFeedCard()
                }
            }

            */
/* ---------- Footer: Append ---------- *//*


            when (pagingItems.loadState.append) {
                is LoadState.Loading -> {
                    item { ShimmerFeedCard() }
                }

                is LoadState.Error -> {
                    item { Text("Failed to load more stories") }
                }

                is LoadState.NotLoading -> Unit
            }

            */
/* ---------- Footer: Refresh Errors ---------- *//*


            when (pagingItems.loadState.refresh) {
                is LoadState.Error -> {
                    item { Text("Failed to load top stories") }
                }

                is LoadState.Loading -> Unit // already handled by pull-to-refresh
                is LoadState.NotLoading -> Unit
            }
        }
    }
}
*/

/*
@Composable
fun TopStoriesScreen(
    viewModel: SharedViewModel = hiltViewModel(),
    onArticleClick: (FeedItem) -> Unit
) {
    val pagingItems = viewModel.topStoriesPaging.collectAsLazyPagingItems()

    *//* ---------- Scroll Restore ---------- *//*

    val savedIndex by viewModel.firstVisibleItemIndex.collectAsState()
    val savedOffset by viewModel.firstVisibleItemScrollOffset.collectAsState()

    val listState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }

    // Restore scroll position when returning to this screen
    LaunchedEffect(Unit) {
        if (savedIndex != 0 || savedOffset != 0) {
            listState.scrollToItem(savedIndex, savedOffset)
        }
    }

    // Save scroll position as user scrolls
    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        viewModel.saveScrollPosition(
            listState.firstVisibleItemIndex,
            listState.firstVisibleItemScrollOffset
        )
    }

    *//* ---------- Pull-to-Refresh ---------- *//*

    val isRefreshing = pagingItems.loadState.refresh is LoadState.Loading
    val pullRefreshState = rememberPullToRefreshState()

    *//* ---------- Initial Empty Handling ---------- *//*

    val isInitialEmpty =
        pagingItems.itemCount == 0 &&
                pagingItems.loadState.refresh is LoadState.NotLoading &&
                !pagingItems.loadState.append.endOfPaginationReached

    PullToRefreshBox(
        state = pullRefreshState,
        isRefreshing = isRefreshing,
        onRefresh = { pagingItems.refresh() }
    ) {

        if (isInitialEmpty) {
            // Avoid blank screen on first emission or back navigation
            LazyColumn(state = listState) {
                items(6) { ShimmerFeedCard() }
            }
            return@PullToRefreshBox
        }

        *//* ---------- Main List ---------- *//*

        LazyColumn(state = listState) {

            items(
                count = pagingItems.itemCount,
                key = { index ->
                    // Stable key: prefer cached item
                    pagingItems.peek(index)?.link ?: index
                }
            ) { index ->

                // Instant rendering when returning to screen
                val item = pagingItems.peek(index) ?: pagingItems[index]

                if (item != null) {
                    AnimatedFeedCard(item) {
                        onArticleClick(item)
                    }
                } else {
                    ShimmerFeedCard()
                }
            }

            *//* ---------- Footer: Append ---------- *//*

            when (pagingItems.loadState.append) {
                is LoadState.Loading -> {
                    item { ShimmerFeedCard() }
                }

                is LoadState.Error -> {
                    item { Text("Failed to load more stories") }
                }

                is LoadState.NotLoading -> Unit
            }

            *//* ---------- Footer: Refresh Errors ---------- *//*

            when (pagingItems.loadState.refresh) {
                is LoadState.Error -> {
                    item { Text("Failed to load top stories") }
                }

                is LoadState.Loading -> Unit // already handled by pull-to-refresh
                is LoadState.NotLoading -> Unit
            }
        }
    }
}*/


/*
@Composable
fun TopStoriesScreen(
    viewModel: SharedViewModel = hiltViewModel(),
    onArticleClick: (FeedItem) -> Unit
) {
    val pagingItems = viewModel.topStoriesPaging.collectAsLazyPagingItems()

    *//* ---------- Scroll Restore ---------- *//*

    val savedIndex by viewModel.firstVisibleItemIndex.collectAsState()
    val savedOffset by viewModel.firstVisibleItemScrollOffset.collectAsState()

    val listState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }

    // Restore scroll position when returning to this screen
    LaunchedEffect(Unit) {
        if (savedIndex != 0 || savedOffset != 0) {
            listState.scrollToItem(savedIndex, savedOffset)
        }
    }

    // Save scroll position as user scrolls
    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        viewModel.saveScrollPosition(
            listState.firstVisibleItemIndex,
            listState.firstVisibleItemScrollOffset
        )
    }

    *//* ---------- Pull-to-Refresh ---------- *//*

    val isRefreshing = pagingItems.loadState.refresh is LoadState.Loading
    val pullRefreshState = rememberPullToRefreshState()

    *//* ---------- Initial Empty Handling ---------- *//*

    val isInitialEmpty =
        pagingItems.itemCount == 0 &&
                pagingItems.loadState.refresh is LoadState.NotLoading &&
                !pagingItems.loadState.append.endOfPaginationReached

    PullToRefreshBox(
        state = pullRefreshState,
        isRefreshing = isRefreshing,
        onRefresh = { pagingItems.refresh() }
    ) {

        if (isInitialEmpty) {
            // Avoid blank screen on first emission or back navigation
            LazyColumn(state = listState) {
                items(6) { ShimmerFeedCard() }
            }
            return@PullToRefreshBox
        }

        *//* ---------- Main List ---------- *//*

        LazyColumn(state = listState) {

            items(
                count = pagingItems.itemCount,
                key = { index ->
                    // Stable key: prefer cached item
                    pagingItems.peek(index)?.link ?: index
                }
            ) { index ->

                // Instant rendering when returning to screen
                val item = pagingItems.peek(index) ?: pagingItems[index]

                if (item != null) {
                    AnimatedFeedCard(item) {
                        onArticleClick(item)
                    }
                } else {
                    ShimmerFeedCard()
                }
            }

            *//* ---------- Footer States ---------- *//*

            *//* ---------- Footer States ---------- *//*

            when (pagingItems.loadState.append) {
                is LoadState.Loading -> {
                    item { ShimmerFeedCard() }
                }

                is LoadState.Error -> {
                    item { Text("Failed to load more stories") }
                }

                is LoadState.NotLoading -> Unit // nothing to show
            }

            when (pagingItems.loadState.refresh) {
                is LoadState.Error -> {
                    item { Text("Failed to load top stories") }
                }

                is LoadState.Loading -> Unit // already handled by pull-to-refresh
                is LoadState.NotLoading -> Unit
            }


        }
    }
}*/


/*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopStoriesScreen(
    viewModel: SharedViewModel = hiltViewModel(),
    onArticleClick: (FeedItem) -> Unit
) {
    val pagingItems = viewModel.topStoriesPaging.collectAsLazyPagingItems()

    val savedIndex by viewModel.firstVisibleItemIndex.collectAsState()
    val savedOffset by viewModel.firstVisibleItemScrollOffset.collectAsState()

    val listState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState() // no args allowed
    }

    LaunchedEffect(Unit) {
        // Restore scroll position only once when screen is first composed
        if (savedIndex != 0 || savedOffset != 0) {
            listState.scrollToItem(savedIndex, savedOffset)
        }
    }


    // Pull-to-refresh state derived from Paging
    val isRefreshing = pagingItems.loadState.refresh is LoadState.Loading
    val pullRefreshState = rememberPullToRefreshState()

    // Detect "initial empty but not really done" state to avoid blank screen
    val isInitialEmpty =
        pagingItems.itemCount == 0 &&
                pagingItems.loadState.refresh is LoadState.NotLoading &&
                !pagingItems.loadState.append.endOfPaginationReached

    PullToRefreshBox(
        state = pullRefreshState,
        isRefreshing = isRefreshing,
        onRefresh = { pagingItems.refresh() }
    ) {
        if (isInitialEmpty) {
            // First emission after coming back: show shimmer instead of blank
            LazyColumn(state = listState) {
                items(5) {
                    ShimmerFeedCard()
                }
            }
        } else {
            LaunchedEffect(
                listState.firstVisibleItemIndex,
                listState.firstVisibleItemScrollOffset
            ) {
                viewModel.saveScrollPosition(
                    listState.firstVisibleItemIndex,
                    listState.firstVisibleItemScrollOffset
                )
            }

            LazyColumn(state = listState) {
                items(
                    count = pagingItems.itemCount,
                    key = { index ->
                        // Prefer cached item for stable key
                        pagingItems.peek(index)?.link ?: index
                    }
                ) { index ->
                    // Use peek() to show cached items immediately when returning to this screen
                    val item = pagingItems.peek(index) ?: pagingItems[index]

                    if (item != null) {
                        AnimatedFeedCard(item) {
                            onArticleClick(item)
                        }
                    } else {
                        ShimmerFeedCard()
                    }
                }

                // Footer states
                when {
                    pagingItems.loadState.append is LoadState.Loading -> {
                        item { ShimmerFeedCard() }
                    }

                    pagingItems.loadState.refresh is LoadState.Error -> {
                        item { Text("Failed to load top stories") }
                    }

                    pagingItems.loadState.append is LoadState.Error -> {
                        item { Text("Failed to load more stories") }
                    }
                }
            }
        }
    }
}
*/


/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopStoriesScreen(
    viewModel: SharedViewModel = hiltViewModel(),
    onArticleClick: (FeedItem) -> Unit
) {
    val pagingItems = viewModel.topStoriesPaging.collectAsLazyPagingItems()
    val listState = rememberLazyListState()
    val pullRefreshState = rememberPullToRefreshState()

    val isRefreshing = pagingItems.loadState.refresh is LoadState.Loading

    PullToRefreshBox(
        state = pullRefreshState,
        isRefreshing = isRefreshing,
        onRefresh = { pagingItems.refresh() }
    ) {
        LazyColumn(state = listState) {

            items(
                count = pagingItems.itemCount,
                key = { pagingItems[it]?.link ?: it }
            ) { index ->
                pagingItems[index]?.let { item ->
                    AnimatedFeedCard(item) { onArticleClick(item) }
                } ?: ShimmerFeedCard()
            }

            when {
                pagingItems.loadState.append is LoadState.Loading ->
                    item { ShimmerFeedCard() }

                pagingItems.loadState.refresh is LoadState.Error ->
                    item { Text("Failed to load top stories") }
            }
        }
    }
}*/


/*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopStoriesScreen(
    viewModel: SharedViewModel = hiltViewModel(),
    onArticleClick: (FeedItem) -> Unit
) {
    val pagingItems = viewModel.topStoriesPaging.collectAsLazyPagingItems()
    val isRefreshing by viewModel.isRefreshingTop.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val pullRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        state = pullRefreshState,
        isRefreshing = isRefreshing,
        onRefresh = {
            pagingItems.refresh()
            viewModel.refreshTopStories()
        }
    ) {
        LazyColumn(state = listState) {

            items(
                count = pagingItems.itemCount,
                key = { pagingItems[it]?.link ?: it }
            ) { index ->
                val item = pagingItems[index]
                if (item != null) {
                    AnimatedFeedCard(item) {
                        onArticleClick(item)
                    }
                } else {
                    ShimmerFeedCard()
                }
            }

            // Footer states
            pagingItems.apply {
                when {
                    loadState.refresh is LoadState.Loading -> {
                        item { ShimmerFeedCard() }
                    }

                    loadState.append is LoadState.Loading -> {
                        item { ShimmerFeedCard() }
                    }

                    loadState.refresh is LoadState.Error -> {
                        item { Text("Failed to load top stories") }
                    }
                }
            }
        }
    }
}

*/

/*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopStoriesScreen(
    viewModel: SharedViewModel = hiltViewModel(),
    onArticleClick: (FeedItem) -> Unit
) {
    val feedState by viewModel.topStoriesState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshingTop.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()

    val hasLoadedOnceTop by viewModel.hasLoadedOnceTop.collectAsStateWithLifecycle()
    val pullRefreshState = rememberPullToRefreshState()


// ✅ Scroll to top when refresh finishes
    val wasRefreshing = remember { mutableStateOf(false) }
    LaunchedEffect(isRefreshing) {
        if (wasRefreshing.value && !isRefreshing) {
            listState.animateScrollToItem(0)
        }
        wasRefreshing.value = isRefreshing
    }

    PullToRefreshBox(
        state = pullRefreshState,
        isRefreshing = isRefreshing,
        onRefresh = viewModel::refreshTopStories,


        indicator = {
            PullToRefreshDefaults.Indicator(
                state = pullRefreshState,
                isRefreshing = isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter)


            )
        }
    ) {
        Column {

            when (feedState) {

                is ResourceState.Loading -> {
                    if (!hasLoadedOnceTop) {
                        ShimmerFeedCard()
                    }
                }

                is ResourceState.Error -> {
                    Text("Error loading top stories")
                }

                is ResourceState.Success -> {
                    val items = (feedState as ResourceState.Success).data
                    LazyColumn(state = listState) {
                        items(items, key = { it.link })

                        { item ->
                            AnimatedFeedCard(item) {
                                onArticleClick(item)
                            }
                        }
                    }
                }
            }
        }
    }
}
*/


/*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopStoriesScreen(
    viewModel: SharedViewModel = hiltViewModel(),
    onArticleClick: (FeedItem) -> Unit
) {
    // Paging 3 items
    val pagingItems = viewModel.topStoriesPaging.collectAsLazyPagingItems()

    // Refresh state
    val isRefreshing by viewModel.isRefreshingTop.collectAsStateWithLifecycle()
    val hasLoadedOnceTop by viewModel.hasLoadedOnceTop.collectAsStateWithLifecycle()

    // Scroll restoration
    val firstVisibleIndex by viewModel.firstVisibleItemIndex.collectAsState()
    val firstVisibleOffset by viewModel.firstVisibleItemScrollOffset.collectAsState()

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = firstVisibleIndex,
        initialFirstVisibleItemScrollOffset = firstVisibleOffset
    )

    // Pull-to-refresh
    val pullRefreshState = rememberPullToRefreshState()

    // Scroll to top after refresh completes
    val wasRefreshing = remember { mutableStateOf(false) }
    LaunchedEffect(isRefreshing) {
        if (wasRefreshing.value && !isRefreshing) {
            listState.animateScrollToItem(0)
        }
        wasRefreshing.value = isRefreshing
    }

    // Save scroll position
    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        viewModel.saveScrollPosition(
            listState.firstVisibleItemIndex,
            listState.firstVisibleItemScrollOffset
        )
    }



    PullToRefreshBox(
        state = pullRefreshState,
        isRefreshing = isRefreshing,
        onRefresh = viewModel::refreshTopStories,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = pullRefreshState,
                isRefreshing = isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    ) {
        Column {

            // Initial shimmer before first page loads
            if (!hasLoadedOnceTop && pagingItems.itemCount == 0) {
                ShimmerFeedCard()
            }

            LazyColumn(state = listState) {

                items(
                    count = pagingItems.itemCount,
                    key = { index ->
                        pagingItems[index]?.link ?: "placeholder-$index"
                    }
                ) { index ->
                    val item = pagingItems[index]

                    if (item != null) {
                        AnimatedFeedCard(item) {
                            onArticleClick(item)
                        }
                    } else {
                        // Paging placeholder while loading
                        ShimmerFeedCard()
                    }
                }


                // Error item at bottom (optional)
                pagingItems.apply {
                    when (loadState.append) {
                        is LoadState.Error -> {
                            item {
                                Text(
                                    "Error loading more stories",
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    // Mark "loaded once" when first page finishes
    LaunchedEffect(pagingItems.loadState.refresh) {
        val state = pagingItems.loadState.refresh
        if (state is LoadState.NotLoading && pagingItems.itemCount > 0) {
            viewModel.setTopStoriesLoadedOnce()
        }
    }
}

*/



*/
