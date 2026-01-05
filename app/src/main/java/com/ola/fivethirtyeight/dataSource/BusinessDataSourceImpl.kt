package com.ola.fivethirtyeight.dataSource

import com.ola.fivethirtyeight.api.ApiService
import com.ola.fivethirtyeight.config.FeedConfig
import com.ola.fivethirtyeight.model.FeedItem
import com.ola.fivethirtyeight.parser.parseRss
import com.ola.fivethirtyeight.utils.extractGoogleImage
import org.jsoup.nodes.Element
import javax.inject.Inject

class BusinessDataSourceImpl @Inject constructor(
    private val apiService: ApiService
) : BusinessDataSource {

    override suspend fun getFeedList(): List<FeedItem> =
        fetchAndParse(
            url = "abcnews/moneyheadlines",
            image = { it.getElementsByTag("media:thumbnail").first()?.attr("url") ?: "" }
        )

    override suspend fun getGoogleBusiness(): List<FeedItem> =
        fetchAndParse(
            url = "https://news.google.com/rss/topics/CAAqJggKIiBDQkFTRWdvSUwyMHZNRGx6TVdZU0FtVnVHZ0pWVXlnQVAB?hl=en-US&gl=US&ceid=US%3Aen",
            image = { extractGoogleImage(it) },
            titleTransform = { it.substringBeforeLast("-") }


        )

    override suspend fun getNyBusiness(): List<FeedItem> =
        fetchAndParse(
            url = "https://rss.nytimes.com/services/xml/rss/nyt/YourMoney.xml",
            image = { it.getElementsByTag("media:content").first()?.attr("url") ?: "" }
        )

    override suspend fun getNprBusiness(): List<FeedItem> =
        fetchAndParse(
            url = "https://feeds.npr.org/1006/rss.xml",
            image = {
                it.getElementsByTag("content:encoded")
                    .first()
                    ?.select("img")
                    ?.attr("src")
                    ?: ""
            }
        )


    private suspend fun fetchAndParse(
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


}








/*

class BusinessDataSourceImpl @Inject constructor(private val apiService: ApiService) :
    BusinessDataSource {


        override suspend  fun getFeedList(): MutableList<FeedItem> {

            val listData = mutableListOf<FeedItem>()


            val responseResult = apiService.getFeedItems("abcnews/moneyheadlines")

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

                    val imgLink = element.getElementsByTag("media:thumbnail").first()

                    var picImage = imgLink?.attr("url") ?: ""

                   */
/* if (picImage.contains("default") || picImage.isEmpty() || picImage.contains("null")) {
                        picImage = R.drawable.picsart.toString()

                    }*//*



                    listData.add(
                        FeedItem(
                            sTitle,
                            sDesc,
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


       override suspend fun getGoogleBusiness(): MutableList<FeedItem> {

            val listData = mutableListOf<FeedItem>()


            val responseResult =
                apiService.getFeedItems("https://news.google.com/rss/topics/CAAqJggKIiBDQkFTRWdvSUwyMHZNRGx6TVdZU0FtVnVHZ0pWVXlnQVAB?hl=en-US&gl=US&ceid=US%3Aen")

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


     override  suspend fun getNyBusiness(): MutableList<FeedItem> {

            val listData = mutableListOf<FeedItem>()


            val responseResult =
                apiService.getFeedItems("https://rss.nytimes.com/services/xml/rss/nyt/YourMoney.xml")

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


    override suspend fun getNprBusiness(): MutableList<FeedItem> {

            val listData = mutableListOf<FeedItem>()


            val responseResult = apiService.getFeedItems("https://feeds.npr.org/1006/rss.xml")

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

                   */
/* val imageUrls = itemElements.mapNotNull { item ->
                        val contentEncoded = item.select("encoded").firstOrNull()?.text()
                        val contentHtml = contentEncoded?.let { Jsoup.parse(it) }
                        contentHtml?.select("img")?.firstOrNull()?.attr("src")
                    }*//*




                   */
/* if (picImage != null) {
                        if (picImage.contains("default") || picImage.isEmpty() || picImage.contains("null")) {
                            picImage = R.drawable.picsart.toString()

                        }
                    }*//*


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
