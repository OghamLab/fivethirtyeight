package com.ola.fivethirtyeight.screens

import com.ola.fivethirtyeight.R


sealed class BottomBarScreen(
    val route: String,
    val title: String,
    val selectedIcon: Int,
    val unselectedIcon: Int


) {
    data object FiveThirtyEightHeadlines : BottomBarScreen(
        route = "silver",
        title = "Silver Bulletin",
        selectedIcon = R.drawable.baseline_home_24,
        unselectedIcon = R.drawable.outline_home_24,


        )




     object News : BottomBarScreen(
        route = "news",
        title = "News",
        selectedIcon = R.drawable.baseline_newspaper_24,
        unselectedIcon = R.drawable.outline_newspaper_24,
    )


    data object LatestPolls : BottomBarScreen(
        route = "latest",
        title = "Latest Polls",
        selectedIcon = R.drawable.baseline_poll_24,
        unselectedIcon = R.drawable.outline_poll_24,
    )

    data object Collections : BottomBarScreen(
        route = "collections",
        title = "Collections",
        selectedIcon = R.drawable.baseline_bookmark_24,
        unselectedIcon = R.drawable.outline_bookmark_24,


        )


    data object Settings : BottomBarScreen(
        route = "settings",
        title = "Settings",
        selectedIcon = R.drawable.baseline_more_vert_24,
        unselectedIcon = R.drawable.outline_more_vert_24,
        )



}
