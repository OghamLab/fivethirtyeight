package com.ola.fivethirtyeight.screens

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.ola.fivethirtyeight.include.SimpleLightTopAppBar
import com.ola.fivethirtyeight.model.FeedItem
import com.ola.fivethirtyeight.resource.ResourceState
import com.ola.fivethirtyeight.utils.AnimatedFeedCard
import com.ola.fivethirtyeight.utils.ShimmerFeedCard
import com.ola.fivethirtyeight.viewmodel.SharedViewModel

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FiveThirtyEightScreen(
    viewModel: SharedViewModel = hiltViewModel(),
    onArticleClick: (FeedItem) -> Unit
) {
    val hasLoadedOnceFive by viewModel.hasLoadedOnceFive.collectAsStateWithLifecycle()
    val feedState by viewModel.fiveThirtyEightFeedState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshingFiveThirtyEight.collectAsState()

    val firstVisibleIndex by viewModel.firstVisibleItemIndex.collectAsState()
    val firstVisibleOffset by viewModel.firstVisibleItemScrollOffset.collectAsState()

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = firstVisibleIndex,
        initialFirstVisibleItemScrollOffset = firstVisibleOffset
    )

    // Save scroll position
    LaunchedEffect(listState) {
        snapshotFlow {
            listState.firstVisibleItemIndex to
                    listState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->
            viewModel.saveScrollPosition(index, offset)
        }
    }

    Scaffold(
        topBar = { SimpleLightTopAppBar() },
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->

        val pullRefreshState = rememberPullToRefreshState()

        PullToRefreshBox(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            state = pullRefreshState,
            isRefreshing = isRefreshing,
            onRefresh = {
                viewModel.fetchFiveThirtyEightStories(isUserRefresh = true)
            },
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullRefreshState,
                    isRefreshing = isRefreshing,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        ) {

            when (feedState) {
                is ResourceState.Loading -> {
                    if (!hasLoadedOnceFive) {
                        ShimmerFeedCard()
                    }
                }

                is ResourceState.Error -> {
                    Text("Error loading top stories")
                }

                is ResourceState.Success -> {
                    val items = (feedState as ResourceState.Success).data

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = items,
                            key = { it.link } // good 👍
                        ) { item ->
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


/*

    Surface(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            state = pullRefreshState,
            onRefresh = { viewModel.fetchFiveThirtyEightStories() },
            isRefreshing = isRefreshing,
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullRefreshState,
                    isRefreshing = isRefreshing,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        ) {
            when (feedState) {
                is ResourceState.Loading -> {
                    if (!hasLoadedOnce) {
                        ShimmerFeedCard()
                    }
                }

                is ResourceState.Success -> {
                    val articles = (feedState as ResourceState.Success).data

                    LazyColumn(
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 8.dp)
                    ) {
                        items(articles.size,key = {
                            articles[it].link }   ) { index ->
                            val item = articles[index]

                            AnimatedFeedCard(
                                item = item,
                                onClick = {
                                    viewModel.selectFeedItem(it)
                                    onArticleClick(it)
                                }
                            )
                        }
                    }
                }

                is ResourceState.Error -> {
                    val error = (feedState as ResourceState.Error).error
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error: $error",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}*/
