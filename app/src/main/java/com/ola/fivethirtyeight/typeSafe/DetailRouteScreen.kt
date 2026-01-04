package com.ola.fivethirtyeight.typeSafe/*

import android.net.Uri
import androidx.navigation.NavController
import com.ola.fivethirtyeight.model.FeedItem

object Routes {

 object Main {
  const val HOME = "home"
  const val TOP_STORIES = "top_stories"
  const val COLLECTIONS = "collections"
  const val SETTINGS = "settings"
  const val POLITICS = "politics"
  const val WORLD = "world"
  const val BUSINESS = "business"
  const val TECH = "tech"
  const val SPORTS = "sports"
  const val HEALTH = "health"
  const val PRIVACY = "privacy"
  const val NEWS = "news"

 }


 object Article {
  const val ROUTE = "article/{title}/{url}"

  fun create(title: String, url: String): String =
   "article/${Uri.encode(title)}/${Uri.encode(url)}"

  const val DEEP_LINK = "fivethirtyeight://article/{title}/{url}"
 }

}


fun NavController.openArticle(item: FeedItem) {
  navigate(Routes.Article.create(item.title, item.link)) {
   launchSingleTop = true
  }
 }



















*/


/*




@Serializable
object PrivacyRouteScreen

*/
/*
@Serializable
object LatestRouteScreen


@Serializable
data class  FiveThirtyEightRouteScreen(val title: String, val url: String)

@Serializable
data class  TopStoriesRouteScreen(val title: String, val url: String)


@Serializable
data class  WorldRouteScreen(val title: String, val url: String)

@Serializable
data class  BusinessRouteScreen(val title: String, val url: String)



@Serializable
data class  TechRouteScreen(val title: String, val url: String)

@Serializable
data class  SportsRouteScreen(val title: String, val url: String)



@Serializable
data class  HealthRouteScreen(val title: String, val url: String)


@Serializable
data class  PoliticsRouteScreen(val title: String, val url: String)
*//*



@Serializable
data class DetailLatestRouteScreen(val url: String)

@Serializable
data class DetailTopStoriesRouteScreen(val title: String?=null, val url: String)


@Serializable
data class DetailSavedArticlesRouteScreen(val url: String)



object Routes {

 object Main {
  const val HOME = "home"
  const val TOP_STORIES = "top_stories"
  const val COLLECTIONS = "collections"
  const val SETTINGS = "settings"
  const val POLITICS = "politics"
  const val WORLD = "world"
  const val BUSINESS = "business"
  const val TECH = "tech"
  const val SPORTS = "sports"
  const val HEALTH = "health"
 }

 object Article {
  const val ROUTE = "article/{title}/{url}"

  fun create(title: String, url: String): String =
   "article/${Uri.encode(title)}/${Uri.encode(url)}"

  const val DEEP_LINK = "fivethirtyeight://article/{title}/{url}"
 }
}


fun NavController.openArticle(item: FeedItem) {
 navigate(Routes.Article.create(item.title, item.link)) {
  launchSingleTop = true
 }
}





*/
