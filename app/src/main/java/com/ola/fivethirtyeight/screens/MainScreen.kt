package com.ola.fivethirtyeight.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ola.fivethirtyeight.routes.Routes
import com.ola.fivethirtyeight.routes.openArticle
import com.ola.fivethirtyeight.tabs.MainTabContainer
import com.ola.fivethirtyeight.ui.theme.TimesNewRoman

@Composable
fun MainScreen(rootNavController: NavHostController) {

    // ✅ Controller that owns bottom-bar routes
    val bottomNavController = rememberNavController()

    // ✅ Observe bottom-nav back stack (NOT root)
    val bottomBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentBottomRoute = bottomBackStackEntry?.destination?.route

    val bottomBarRoutes = screens.map { it.route }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = currentBottomRoute in bottomBarRoutes,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                // ✅ PASS BOTTOM NAV CONTROLLER
                BottomNavBar(navController = bottomNavController)
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = bottomNavController,
            startDestination = BottomBarScreen.FiveThirtyEightHeadlines.route,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable(BottomBarScreen.FiveThirtyEightHeadlines.route) {
                FiveThirtyEightScreen(onArticleClick = rootNavController::openArticle)
            }

            composable(BottomBarScreen.News.route) {
                MainTabContainer(
                    onArticleClick = rootNavController::openArticle
                )
            }

            composable(BottomBarScreen.LatestPolls.route) {
                LatestPollsScreen(rootNavController)
            }





            composable(BottomBarScreen.Collections.route) {
                SavedArticlesScreen(
                    onArticleClick = rootNavController::openArticle
                )
            }

            composable(BottomBarScreen.Settings.route) {
                SettingsScreen(navController = rootNavController)
            }

            // 🔹 Feed sections (still bottom-nav owned)
            composable(Routes.Main.TOP_STORIES) {
                TopStoriesScreen(onArticleClick = rootNavController::openArticle)
            }

            composable(Routes.Main.POLITICS) {
                PoliticsScreen(onArticleClick = rootNavController::openArticle)
            }

            composable(Routes.Main.WORLD) {
                WorldScreen(onArticleClick = rootNavController::openArticle)
            }

            composable(Routes.Main.BUSINESS) {
                BusinessScreen(onArticleClick = rootNavController::openArticle)
            }

            composable(Routes.Main.TECH) {
                TechScreen(onArticleClick = rootNavController::openArticle)
            }

            composable(Routes.Main.SPORTS) {
                SportsScreen(onArticleClick = rootNavController::openArticle)
            }

            composable(Routes.Main.HEALTH) {
                HealthScreen(onArticleClick = rootNavController::openArticle )
            }
        }
    }
}



@Composable
fun BottomNavBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(containerColor = MaterialTheme.colorScheme.primaryContainer) {
        screens.forEach { screen ->
            val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

            // Animate icon size
            val iconSize by animateDpAsState(
                targetValue = if (selected) 28.dp else 24.dp,
                label = "iconSize"
            )

            // Animate font size
            val fontSize by animateDpAsState(
                targetValue = if (selected) 11.dp else 11.dp,
                label = "fontSize"
            )

            // Animate alpha for subtle fade
            val alpha by animateFloatAsState(
                targetValue = if (selected) 1f else 0.7f,
                label = "alpha"
            )

            NavigationBarItem(
                label = {
                    Text(
                        text = screen.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = TimesNewRoman, // 👈 your custom font
                            fontSize = with(LocalDensity.current) { fontSize.toSp() },
                            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold
                        ),
                        modifier = Modifier.alpha(alpha)
                    )
                },
                selected = selected,
                onClick = {
                    if (currentDestination?.route != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(
                            id = if (selected) screen.selectedIcon else screen.unselectedIcon
                        ),
                        contentDescription = screen.title,
                        modifier = Modifier.size(iconSize) // 👈 animated size
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onBackground,
                    selectedTextColor = MaterialTheme.colorScheme.onBackground,
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onBackground,
                    unselectedTextColor = MaterialTheme.colorScheme.onBackground,
                    disabledIconColor = MaterialTheme.colorScheme.onBackground,
                    disabledTextColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    }
}




val screens = listOf(
    BottomBarScreen.FiveThirtyEightHeadlines,
    BottomBarScreen.LatestPolls,
    BottomBarScreen.News,
    BottomBarScreen.Collections,
    BottomBarScreen.Settings
)



val bottomBarTextStyle = TextStyle(
    fontSize = 16.sp,
    fontWeight = FontWeight.SemiBold,



    )
