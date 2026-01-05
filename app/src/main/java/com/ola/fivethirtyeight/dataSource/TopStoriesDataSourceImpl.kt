package com.ola.fivethirtyeight.dataSource

import com.ola.fivethirtyeight.api.ApiService
import com.ola.fivethirtyeight.config.FeedConfig
import com.ola.fivethirtyeight.config.TOP_STORY_FEEDS
import com.ola.fivethirtyeight.model.FeedItem
import com.ola.fivethirtyeight.parser.parseRss
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject


class TopStoriesDataSourceImpl @Inject constructor(
    private val apiService: ApiService
) : TopStoriesDataSource {

    override suspend fun fetchAllFeeds(): List<FeedItem> = coroutineScope {
        TOP_STORY_FEEDS
            .map { feed ->
                async { fetch(feed) }
            }
            .flatMap { it.await() }
    }

    override suspend fun fetchFeed(name: String): List<FeedItem> {
        val feed = TOP_STORY_FEEDS.firstOrNull { it.name == name }
            ?: return emptyList()
        return fetch(feed)
    }

    private suspend fun fetch(feed: FeedConfig): List<FeedItem> {
        val response = apiService.getFeedItems(feed.url)
        if (!response.isSuccessful) return emptyList()

        return parseRss(
            xml = response.body().toString(),
            imageExtractor = feed.imageExtractor,
            titleTransformer = feed.titleTransformer
        )
    }
}


/*
class TopStoriesDataSourceImpl @Inject constructor(
    private val apiService: ApiService
) : TopStoriesDataSource {

    override suspend fun getFeedList(): List<FeedItem> =
        fetchAndParse(
            url = "abcnews/topstories",
            image = { it.getElementsByTag("media:thumbnail").first()?.attr("url") ?: "" }
        )

    override suspend fun getGoogleTop(): List<FeedItem> =
        fetchAndParse(
            url = "https://news.google.com/rss/topics/CAAqJggKIiBDQkFTRWdvSUwyMHZNRFZxYUdjU0FtVnVHZ0pWVXlnQVAB?hl=en-US&gl=US&ceid=US%3Aen",
            image = { extractGoogleImage(it) },
            titleTransform = { it.substringBeforeLast("-") }


        )

    override suspend fun getNyTop(): List<FeedItem> =
        fetchAndParse(
            url = "https://rss.nytimes.com/services/xml/rss/nyt/US.xml",
            image = { it.getElementsByTag("media:content").first()?.attr("url") ?: "" }
        )

    override suspend fun getNprTop(): List<FeedItem> =
        fetchAndParse(
            url = "https://feeds.npr.org/1001/rss.xml",
            image = {
                it.getElementsByTag("content:encoded")
                    .first()
                    ?.select("img")
                    ?.attr("src")
                    ?: ""
            }
        )


*/











    /*private suspend fun fetchAndParse(
        url: String,
        image: (Element) -> String = { "" },
        titleTransform: (String) -> String = { it }
    ): List<FeedItem> {
        val response = apiService.getFeedItems(url)
        if (!response.isSuccessful) return emptyList()

        return parseRss(
            xml = response.body().toString(),
            imageExtractor = image,
            titleTransformer = titleTransform
        )
    }



    private suspend fun fetchFeed(feed: FeedConfig): List<FeedItem> {
        val response = apiService.getFeedItems(feed.url)
        if (!response.isSuccessful) return emptyList()

        return parseRss(
            xml = response.body().toString(),
            imageExtractor = feed.imageExtractor,
            titleTransformer = feed.titleTransformer
        )
    }






}*/











/*
class TopStoriesDataSourceImpl @Inject constructor(private val apiService: ApiService) :
    TopStoriesDataSource {


    override suspend fun getFeedList(): List<FeedItem> {

        val listData = mutableListOf<FeedItem>() // ✅ internal mutability

        val responseResult = apiService.getFeedItems("abcnews/topstories")
        if (!responseResult.isSuccessful) return emptyList()

        val doc = Jsoup.parse(responseResult.body().toString(), "", Parser.xmlParser())
        val itemElements = doc.select("item")

        val sdf = SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH)

        for (element in itemElements) {

            val sTitle = element.select("title").text()

            val sDesc = Jsoup
                .parse(element.select("description").text())
                .text()

            val sDate = element.select("pubDate").text()
            val timeInMil = sdf.parse(sDate)?.time ?: continue

            val sLink = element.select("link").text()

            val imgLink = element.getElementsByTag("media:thumbnail").first()
            val picImage = imgLink?.attr("url") ?: ""

            listData.add(
                FeedItem(
                     sTitle,
                   sDesc,
                     "",
                     "",
                     sDate,
                     picImage,
                    sLink,
                   "",
                     timeInMil
                )
            )
        }

        return listData // ✅ returned as List<FeedItem>
    }


    override suspend fun getGoogleTop(): MutableList<FeedItem> {

        val listData = mutableListOf<FeedItem>()


        val responseResult =
            apiService.getFeedItems("https://news.google.com/rss/topics/CAAqJggKIiBDQkFTRWdvSUwyMHZNRFZxYUdjU0FtVnVHZ0pWVXlnQVAB?hl=en-US&gl=US&ceid=US%3Aen")

        val doc = Jsoup.parse(responseResult.body().toString(), "", Parser.xmlParser())

        val itemElements: Elements = doc.select(("item"))
        if (responseResult.isSuccessful) {

            for (element in itemElements) {

                val title = element.select("title")
                val sTitle = title.text()
                val sTitle1=sTitle.substringBeforeLast("-")


                val description = element.select("description")
                val sDescription = description.text()
                val doc4 = Jsoup.parse(sDescription)
                val sDesc = doc4.text()


                val date = element.select("pubDate")
                val sDate = date.text()


                val sdf = SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH)

                val currentDate: String = sDate


                val date1 = sdf.parse(currentDate)
                val calendar = Calendar.getInstance()

                calendar.time = date1!!




                val timeInMil = calendar.timeInMillis


                val link = element.select("link")
                val sLink = link.text()

                System.out.println("links  " + sLink)

                val responseResult2 = apiService.getFeedItems(sLink)
                val doc3 = Jsoup.parse(responseResult2.body().toString())

                val imgLink = element.getElementsByTag("media:thumbnail").first()

                var picImage = imgLink?.attr("url") ?: ""

                */
/*if (picImage.contains("default") || picImage.isEmpty() || picImage.contains("null")) {
                    picImage = R.drawable.picsart.toString()

                }*//*



                listData.add(
                    FeedItem(
                        sTitle1,
                        sDesc,
                        "",   //contentText2.toString(),
                        "",  // sAuthor,
                        sDate,
                        "",
                        sLink,
                        "",
                        timeInMil

                    )
                )

            }


        } else {
            println("Unexpected error occurred. Check your internet connection.")
        }
        return listData

    }


    override  suspend fun getNyTop(): MutableList<FeedItem> {

        val listData = mutableListOf<FeedItem>()


        val responseResult =
            apiService.getFeedItems("https://rss.nytimes.com/services/xml/rss/nyt/US.xml")

        val doc = Jsoup.parse(responseResult.body().toString(), "", Parser.xmlParser())

        val itemElements: Elements = doc.select(("item"))
        if (responseResult.isSuccessful) {

            for (element in itemElements) {

                val title = element.select("title")
                val sTitle = title.text()


                val description = element.select("description")
                val sDescription = description.text()
                val doc4 = Jsoup.parse(sDescription)
                val sDesc = doc4.text()


                val date = element.select("pubDate")
                val sDate = date.text()


                val sdf = SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH)

                val currentDate: String = sDate


                val date1 = sdf.parse(currentDate)
                val calendar = Calendar.getInstance()

                calendar.time = date1!!


                val timeInMil = calendar.timeInMillis


                val link = element.select("link")
                val sLink = link.text()

                System.out.println("links  " + sLink)

                val responseResult2 = apiService.getFeedItems(sLink)
                val doc3 = Jsoup.parse(responseResult2.body().toString())

                val imgLink = element.getElementsByTag("media:content").first()

                var picImage = imgLink?.attr("url") ?: ""

                */
/* if (picImage.contains("default") || picImage.isEmpty() || picImage.contains("null")) {
                     picImage = R.drawable.picsart.toString()

                 }
*//*


                listData.add(
                    FeedItem(
                        sTitle,
                        "",
                        "",   //contentText2.toString(),
                        "",  // sAuthor,
                        sDate,
                        picImage,
                        sLink,
                        "",
                        timeInMil

                    )
                )

            }


        } else {
            println("Unexpected error occurred. Check your internet connection.")
        }
        return listData

    }


    override suspend fun getNprTop(): MutableList<FeedItem> {

        val listData = mutableListOf<FeedItem>()


        val responseResult = apiService.getFeedItems("https://feeds.npr.org/1001/rss.xml")

        val doc = Jsoup.parse(responseResult.body().toString(), "", Parser.xmlParser())

        val itemElements: Elements = doc.select(("item"))
        if (responseResult.isSuccessful) {

            for (element in itemElements) {

                val title = element.select("title")
                val sTitle = title.text()


                val description = element.select("description")
                val sDescription = description.text()
                val doc4 = Jsoup.parse(sDescription)
                val sDesc = doc4.text()


                val date = element.select("pubDate")
                val sDate = date.text()


                val sdf = SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH)

                val currentDate: String = sDate


                val date1 = sdf.parse(currentDate)
                val calendar = Calendar.getInstance()

                calendar.time = date1!!


                val timeInMil = calendar.timeInMillis


                val link = element.select("link")
                val sLink = link.text()

                System.out.println("links  " + sLink)

                val responseResult2 = apiService.getFeedItems(sLink)
                val doc3 = Jsoup.parse(responseResult2.body().toString())

                val imgLink = element.getElementsByTag("content:encoded").first()

                var picImage = imgLink?.select("img")?.attr("src")


                listData.add(
                    FeedItem(
                        sTitle,
                        "",
                        "",   //contentText2.toString(),
                        "",  // sAuthor,
                        sDate,
                        picImage.toString(),
                        sLink,
                        "",
                        timeInMil

                    )
                )

            }


        } else {
            println("Unexpected error occurred. Check your internet connection.")
        }
        return listData

    }







}



*/









