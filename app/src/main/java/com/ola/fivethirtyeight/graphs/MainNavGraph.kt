package com.ola.fivethirtyeight.graphs

/*

@Composable
fun MainNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = BottomBarScreen.FiveThirtyEightHeadlines.route,
        modifier = modifier
    ) {





        composable(BottomBarScreen.News.route) {
            MainTabContainer(
                onArticleClick = navController::openArticle
            )
        }

        composable(BottomBarScreen.LatestPolls.route) {
            LatestPollsScreen(navController = navController)
        }

        composable(BottomBarScreen.Collections.route) {
            SavedArticlesScreen(
                onArticleClick = navController::openArticle
            )
        }

        composable(BottomBarScreen.Settings.route) {
            SettingsScreen(navController = navController)
        }

        composable(BottomBarScreen.FiveThirtyEightHeadlines.route) {
            val vm: TopStoriesViewModel = hiltViewModel()
            FiveThirtyEightScreen (
                viewModel = vm,
                onArticleClick = navController::openArticle
            )
        }



        composable(Routes.Main.TOP_STORIES) {
            val vm: TopStoriesViewModel = hiltViewModel()
            TopStoriesScreen(
                viewModel = vm,
                onArticleClick = navController::openArticle
            )
        }

        composable(Routes.Main.COLLECTIONS) {
            SavedArticlesScreen(
                onArticleClick = navController::openArticle
            )
        }

        composable(Routes.Main.POLITICS) {
            val vm: TopStoriesViewModel = hiltViewModel()
            PoliticsScreen(
                viewModel = vm,
                onArticleClick = navController::openArticle
            )
        }

        composable(Routes.Main.WORLD) {
            val vm: TopStoriesViewModel = hiltViewModel()
            WorldScreen(
                viewModel = vm,
                onArticleClick = navController::openArticle
            )
        }

        composable(Routes.Main.BUSINESS) {
            val vm: TopStoriesViewModel = hiltViewModel()
            BusinessScreen(
                viewModel = vm,
                onArticleClick = navController::openArticle
            )
        }

        composable(Routes.Main.TECH) {
            val vm: TopStoriesViewModel = hiltViewModel()
            TechScreen(
                viewModel = vm,
                onArticleClick = navController::openArticle
            )
        }

        composable(Routes.Main.SPORTS) {
            val vm: TopStoriesViewModel = hiltViewModel()
            SportsScreen(
                viewModel = vm,
                onArticleClick = navController::openArticle
            )
        }

        composable(Routes.Main.HEALTH) {
            val vm: TopStoriesViewModel = hiltViewModel()
            HealthScreen(
                viewModel = vm,
                onArticleClick = navController::openArticle
            )
        }

        composable(Routes.Main.SETTINGS) {
            SettingsScreen(navController = navController)
        }

        // 🔥 ARTICLE DETAIL (deep-link enabled)
        composable(
            route = Routes.Article.ROUTE,
            arguments = listOf(
                navArgument("title") { type = NavType.StringType },
                navArgument("url") { type = NavType.StringType }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = Routes.Article.DEEP_LINK }
            )
        ) { backStackEntry ->
            val title = Uri.decode(backStackEntry.arguments?.getString("title") ?: "")
            val url = Uri.decode(backStackEntry.arguments?.getString("url") ?: "")

            if (url.isNotBlank()) {
                ArticleDetailScreen(
                    title = title,
                    url = url,
                    onBackClick = { navController.popBackStack() }
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Unable to load article.")
                }
            }
        }
    }
}
*/











/*

@Composable
fun MainNavGraph(
    navController: NavHostController,
    modifier: Modifier

    ) {


    NavHost(
        navController = navController,
        startDestination = BottomBarScreen.FiveThirtyEightHeadlines.route,
        modifier = modifier
    )

    {

        composable(route = BottomBarScreen.FiveThirtyEightHeadlines.route) {
            FiveThirtyEightScreen {
                navController.navigate(
                    DetailTopStoriesRouteScreen(
                        Uri.encode(it.title),
                        Uri.encode(it.link)
                    )
                )
            }
        }



        composable(route = BottomBarScreen.Collections.route) {
            SavedArticlesScreen(
                onArticleClick = { item ->
                    navController.navigate(
                        DetailTopStoriesRouteScreen(
                            Uri.encode(item.title),
                            Uri.encode(item.link)
                        )
                    )
                }
            )
        }


        composable<FiveThirtyEightRouteScreen> {
            val vm: TopStoriesViewModel = hiltViewModel()
            TopStoriesScreen(
                viewModel=vm,
                onArticleClick = { item ->
                    vm.selectFeedItem(item)
                    navController.navigate(
                        DetailTopStoriesRouteScreen(
                            Uri.encode(item.title),
                            Uri.encode(item.link)
                        )
                    )
                }
            )
        }

        composable<TopStoriesRouteScreen> {
            val vm: TopStoriesViewModel = hiltViewModel()
            TopStoriesScreen(
                viewModel = vm,
                onArticleClick = { item ->
                    vm.selectFeedItem(item)
                    navController.navigate(
                        DetailTopStoriesRouteScreen(
                            "",
                            Uri.encode(item.link)
                        )
                    )
                }
            )
        }


        composable<PoliticsRouteScreen> {
            val vm: TopStoriesViewModel = hiltViewModel()
            PoliticsScreen(
                viewModel = vm,
                onArticleClick = { item ->
                    vm.selectFeedItem(item)
                    navController.navigate(
                        DetailTopStoriesRouteScreen(
                            Uri.encode(item.title),
                            Uri.encode(item.link)
                        )
                    )
                }
            )
        }



        composable<WorldRouteScreen> {
            val vm: TopStoriesViewModel = hiltViewModel()
            WorldScreen(
                viewModel = vm,
                onArticleClick = { item ->
                    vm.selectFeedItem(item)
                    navController.navigate(
                        DetailTopStoriesRouteScreen(
                            Uri.encode(item.title),
                            Uri.encode(item.link)
                        )
                    )
                }
            )
        }

        composable<BusinessRouteScreen> {
            val vm: TopStoriesViewModel = hiltViewModel()
            BusinessScreen(
                viewModel = vm,
                onArticleClick = { item ->
                    vm.selectFeedItem(item)
                    navController.navigate(
                        DetailTopStoriesRouteScreen(
                            Uri.encode(item.title),
                            Uri.encode(item.link)
                        )
                    )
                }
            )
        }

        composable<TechRouteScreen> {
            val vm: TopStoriesViewModel = hiltViewModel()
            TechScreen(
                viewModel = vm,
                onArticleClick = { item ->
                    vm.selectFeedItem(item)
                    navController.navigate(
                        DetailTopStoriesRouteScreen(
                            Uri.encode(item.title),
                            Uri.encode(item.link)
                        )
                    )
                }
            )
        }

        composable<SportsRouteScreen> {
            val vm: TopStoriesViewModel = hiltViewModel()
            SportsScreen(
                viewModel = vm,
                onArticleClick = { item ->
                    vm.selectFeedItem(item)
                    navController.navigate(
                        DetailTopStoriesRouteScreen(
                            Uri.encode(item.title),
                            Uri.encode(item.link)
                        )
                    )
                }
            )
        }

        composable<HealthRouteScreen> {
            val vm: TopStoriesViewModel = hiltViewModel()
            HealthScreen(
                viewModel = vm,
                onArticleClick = { item ->
                    vm.selectFeedItem(item)
                    navController.navigate(
                        DetailTopStoriesRouteScreen(
                            Uri.encode(item.title),
                            Uri.encode(item.link)
                        )
                    )
                }
            )
        }


        */
/*  composable<DetailSavedArticlesRouteScreen> {
            val vm: TopStoriesViewModel = hiltViewModel()
            TopStoriesScreen(
                viewModel=vm,
                onArticleClick = { item ->
                    vm.selectFeedItem(item)
                    navController.navigate(
                        DetailTopStoriesRouteScreen(
                            Uri.encode(item.title),
                            Uri.encode(item.link)
                        )
                    )
                }
            )
        }*//*





        composable<DetailTopStoriesRouteScreen> { backStackEntry ->
            val args = backStackEntry.toRoute<DetailTopStoriesRouteScreen>()
            val decodedUrl = Uri.decode(args.url)
            val decodedTitle = Uri.decode(args.title)
            if (decodedUrl.isNotBlank()) {
                ArticleDetailScreen(
                    url = decodedUrl,
                    title = "",
                    onBackClick = { navController.popBackStack() })
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Unable to load article.")
                }
            }
        }


        composable(route = BottomBarScreen.LatestPolls.route) {
            LatestPollsScreen(navController = navController)

        }


        composable(route = BottomBarScreen.News.route) {
            MainTabContainer(onArticleClick = {
                navController.navigate(
                    DetailTopStoriesRouteScreen(
                        Uri.encode(it.title),
                        Uri.encode(it.link)
                    )
                )

            })
        }




        composable<DetailLatestRouteScreen> { backStackEntry ->
            val args = backStackEntry.toRoute<DetailLatestRouteScreen>()
            LatestPollsDetailScreen(args.url, navController)

        }


        */
/*composable<LatestRouteScreen> {
            LatestPollsScreen(navController)

        }*//*



        composable<PrivacyRouteScreen> {

            PrivacyScreen(navController)


        }


        composable(route = BottomBarScreen.Settings.route) {

            val vm: SettingsViewModel = hiltViewModel()

            SettingsScreen(viewModel = vm, navController = navController)


        }


    }

}


*/
