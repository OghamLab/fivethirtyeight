package com.ola.fivethirtyeight.routes

import android.net.Uri
import androidx.navigation.NavController
import com.ola.fivethirtyeight.model.FeedItem


object Routes {

    object Main {
        const val ROOT = "main"
        const val HOME = "home" // fivethirtyeight headlines

        const val TOP_STORIES = "top_stories"
        const val POLITICS = "politics"
        const val WORLD = "world"
        const val BUSINESS = "business"
        const val TECH = "tech"
        const val SPORTS = "sports"
        const val HEALTH = "health"
        const val PRIVACY = "privacy"
        const val SPLASH = "splash"


    }

    object Article {
        const val ROUTE = "article/{title}/{url}"

        fun create(title: String, url: String): String =
            "article/${Uri.encode(title)}/${Uri.encode(url)}"

        const val DEEP_LINK = "fivethirtyeight://article/{title}/{url}"
    }

    object LatestPollDetail {
        const val ROUTE = "latest_poll_detail/{encodedUrl}"
        fun create(url: String): String = "latest_poll_detail/${Uri.encode(url)}"

    }




}


fun NavController.openLatestPoll(url: String) {
    navigate(Routes.LatestPollDetail.create(url)) {
        launchSingleTop = true
    }
}


fun NavController.openArticle(item: FeedItem) {
        navigate(Routes.Article.create(item.title, item.link)) {
            launchSingleTop = true
        }


    }
