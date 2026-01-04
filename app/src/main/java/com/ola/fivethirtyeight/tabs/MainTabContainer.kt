package com.ola.fivethirtyeight.tabs

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ola.fivethirtyeight.include.TabSimpleLightTopAppBar
import com.ola.fivethirtyeight.model.FeedItem
import com.ola.fivethirtyeight.screens.BusinessScreen
import com.ola.fivethirtyeight.screens.HealthScreen
import com.ola.fivethirtyeight.screens.PoliticsScreen
import com.ola.fivethirtyeight.screens.SportsScreen
import com.ola.fivethirtyeight.screens.TechScreen
import com.ola.fivethirtyeight.screens.TopStoriesScreen
import com.ola.fivethirtyeight.screens.WorldScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MainTabContainer(onArticleClick: (FeedItem) -> Unit) {


    val tabRowItems = listOf(
        ImageTabItem("Top Stories") { TopStoriesScreen(onArticleClick = onArticleClick) },
        ImageTabItem("Politics") { PoliticsScreen(onArticleClick = onArticleClick) },
        ImageTabItem("World") { WorldScreen(onArticleClick = onArticleClick) },
        ImageTabItem("Business") { BusinessScreen(onArticleClick = onArticleClick) },
        ImageTabItem("Tech") { TechScreen(onArticleClick = onArticleClick) },
        ImageTabItem("Sports") { SportsScreen(onArticleClick = onArticleClick) },
        ImageTabItem("Health & Science") { HealthScreen(onArticleClick = onArticleClick) }
    )


    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(listState)
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { tabRowItems.size }
    )

    /*LaunchedEffect(pagerState.currentPage)
    { listState.animateScrollToItem(pagerState.currentPage) }*/

    LaunchedEffect(pagerState.currentPage) {
        val index = pagerState.currentPage
        val itemOffset = listState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.index == index }?.offset ?: 0

        val itemSize = listState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.index == index }?.size ?: 0

        val viewportCenter = listState.layoutInfo.viewportEndOffset / 2
        val target = itemOffset - viewportCenter + (itemSize / 2)

        listState.animateScrollBy(target.toFloat())
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),

        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                TabSimpleLightTopAppBar(
                    title = "News",
                    modifier = Modifier.fillMaxSize()

                )

            }
        }
    ) { paddingValues ->


        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.tertiary)
                .padding(paddingValues)
                    .padding (top = 16.dp)
        ) {

           // Spacer(modifier = Modifier.height(16.dp))

            // 🔵 Suggestion Chips instead of Tabs
            LazyRow(
                state = listState,
                flingBehavior = flingBehavior,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(tabRowItems) { index, item ->

                    val selected = pagerState.currentPage == index

                    val bgColor by animateColorAsState(
                        targetValue = if (selected)
                            MaterialTheme.colorScheme.secondary
                        else
                            MaterialTheme.colorScheme.tertiary,
                        label = "chip-bg"
                    )

                    val textColor by animateColorAsState(
                        targetValue = if (selected)
                            MaterialTheme.colorScheme.onBackground
                        else
                            MaterialTheme.colorScheme.onBackground,
                        label = "chip-text"
                    )


                    // Animate alpha for subtle fade
                    val alpha by animateFloatAsState(
                        targetValue = if (selected) 1f else 0.7f,
                        label = "alpha"
                    )

                    SuggestionChip(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                                listState.animateScrollToItem(index)
                            }
                        },
                        label = {
                            Text(
                                item.text,
                                fontSize = 18.sp,
                                fontWeight = if (selected) FontWeight.W900 else FontWeight.W600,
                                color = if (selected)
                                    MaterialTheme.colorScheme.onBackground
                                else
                                    MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.alpha(alpha)


                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = bgColor, labelColor = textColor

                        ),
                        border = null

                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // 🔵 Pager stays the same
            HorizontalPager(state = pagerState) {
                tabRowItems[pagerState.currentPage].screen()
            }
        }
    }
}


/*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MainTabContainer(onArticleClick: (FeedItem) -> Unit) {


    val tabRowItems = listOf(
        ImageTabItem(
            text = "Top Stories",
            screen = { TopStoriesScreen(onArticleClick = onArticleClick) }
        ),
        ImageTabItem(
            text = "Politics",
            screen = { PoliticsScreen(onArticleClick = onArticleClick) }
        ),


        ImageTabItem(
            text = "World",
            screen = { WorldScreen(onArticleClick = onArticleClick) }),


        ImageTabItem(
            text = "Business",
            screen = { BusinessScreen(onArticleClick = onArticleClick) }),


        ImageTabItem(
            text = "Tech",
            screen = { TechScreen(onArticleClick = onArticleClick) }),


        ImageTabItem(
            text = "Sports",
            screen = { SportsScreen(onArticleClick = onArticleClick) }),


        ImageTabItem(
            text = "Health & Science",
            screen = { HealthScreen(onArticleClick = onArticleClick) })
    )


    val scope = rememberCoroutineScope()//will use for animation
    val pagerState =
        rememberPagerState(initialPage = 0, pageCount = { tabRowItems.size })//store page state

    val selectedTabIndex = pagerState.currentPage
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp) // 👈 Reduced height (default is 64.dp)
            ) {
                TabSimpleLightTopAppBar(
                    title = "News",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    )


    { paddingValues ->


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {


            ScrollableTabRow(
                edgePadding = 0.dp, selectedTabIndex = pagerState.currentPage, modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 0.dp, bottom = 4.dp)


            ) {

                tabRowItems.forEachIndexed { index, imageTabItem ->

                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick =
                            {
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                        text = {
                            Text(
                                text = imageTabItem.text,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = if (selectedTabIndex == index)
                                        FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (selectedTabIndex == index)
                                    MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                            )
                        },
                        icon = { })
                }

            }

            HorizontalPager(state = pagerState) {
                tabRowItems[pagerState.currentPage].screen()
            }
        }
    }
}



















*/
