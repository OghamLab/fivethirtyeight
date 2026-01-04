package com.ola.fivethirtyeight.graphs

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.ola.fivethirtyeight.detail.ArticleDetailScreen
import com.ola.fivethirtyeight.detail.LatestPollsDetailScreen
import com.ola.fivethirtyeight.routes.Routes
import com.ola.fivethirtyeight.screens.MainScreen
import com.ola.fivethirtyeight.screens.PrivacyScreen
import com.ola.fivethirtyeight.screens.SplashScreen

/*

@Composable
fun RootNavigationGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Main.SPLASH
    ) {

        */
/* ---------------- SPLASH ---------------- *//*

        composable(Routes.Main.SPLASH) {
            SplashScreen(
                onFinished = {
                    navController.navigate(Routes.Main.HOME) {
                        popUpTo(Routes.Main.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        */
/* ---------------- MAIN ENTRY ---------------- *//*

        composable(Routes.Main.HOME) {
           FadeInTransition {
               MainScreen(navController = navController)
           }

        }

        */
/* ---------------- BOTTOM BAR SCREENS ---------------- *//*


        composable(BottomBarScreen.FiveThirtyEightHeadlines.route) {
            FiveThirtyEightScreen(
                viewModel = hiltViewModel(),
                onArticleClick = navController::openArticle
            )
        }

        composable(BottomBarScreen.News.route) {
            MainTabContainer(
                onArticleClick = navController::openArticle
            )
        }

        composable(BottomBarScreen.LatestPolls.route) {
            LatestPollsScreen(navController)
        }

        composable(BottomBarScreen.Collections.route) {
            SavedArticlesScreen(
                onArticleClick = navController::openArticle
            )
        }

        composable(BottomBarScreen.Settings.route) {
            SettingsScreen(navController = navController)
        }

        */
/* ---------------- FEED SECTIONS ---------------- *//*


        composable(Routes.Main.TOP_STORIES) {
            TopStoriesScreen(
                viewModel = hiltViewModel(),
                onArticleClick = navController::openArticle
            )
        }

        composable(Routes.Main.POLITICS) {
            PoliticsScreen(
                viewModel = hiltViewModel(),
                onArticleClick = navController::openArticle
            )
        }

        composable(Routes.Main.WORLD) {
            WorldScreen(
                viewModel = hiltViewModel(),
                onArticleClick = navController::openArticle
            )
        }

        composable(Routes.Main.BUSINESS) {
            BusinessScreen(
                viewModel = hiltViewModel(),
                onArticleClick = navController::openArticle
            )
        }

        composable(Routes.Main.TECH) {
            TechScreen(
                viewModel = hiltViewModel(),
                onArticleClick = navController::openArticle
            )
        }

        composable(Routes.Main.SPORTS) {
            SportsScreen(
                viewModel = hiltViewModel(),
                onArticleClick = navController::openArticle
            )
        }

        composable(Routes.Main.HEALTH) {
            HealthScreen(
                viewModel = hiltViewModel(),
                onArticleClick = navController::openArticle
            )
        }

        */
/* ---------------- STATIC SCREENS ---------------- *//*


        composable(Routes.Main.PRIVACY) {
            PrivacyScreen(navController)
        }

        */
/* ---------------- ARTICLE DETAIL (GLOBAL) ---------------- *//*


        composable(
            route = Routes.Article.ROUTE,
            deepLinks = listOf(
                navDeepLink { uriPattern = Routes.Article.DEEP_LINK }
            )
        ) { backStackEntry ->
            ArticleDetailScreen(
                title = Uri.decode(backStackEntry.arguments?.getString("title") ?: ""),
                url = Uri.decode(backStackEntry.arguments?.getString("url") ?: ""),
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
*/
@Composable
fun RootNavigationGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Main.SPLASH
    ) {

        composable(Routes.Main.SPLASH) {
            SplashScreen {
                navController.navigate(Routes.Main.HOME) {
                    popUpTo(Routes.Main.SPLASH) { inclusive = true }
                }
            }
        }

        composable(Routes.Main.HOME) {
            MainScreen(navController)
        }

        composable(Routes.Main.PRIVACY) {
            PrivacyScreen(navController)
        }




        composable(
            route = Routes.Article.ROUTE,
            deepLinks = listOf(
                navDeepLink { uriPattern = Routes.Article.DEEP_LINK }
            )
        ) { entry ->
            ArticleDetailScreen(
                title = Uri.decode(entry.arguments?.getString("title") ?: ""),
                url = Uri.decode(entry.arguments?.getString("url") ?: ""),
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.LatestPollDetail.ROUTE,
            arguments = listOf(
                navArgument("encodedUrl") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("encodedUrl") ?: ""
            LatestPollsDetailScreen(encodedUrl = encodedUrl, navController = navController)
        }


    }






}
