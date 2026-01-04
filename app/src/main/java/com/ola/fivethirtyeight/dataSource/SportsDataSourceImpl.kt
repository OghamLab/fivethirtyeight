package com.ola.fivethirtyeight.dataSource

import com.ola.fivethirtyeight.api.ApiService
import com.ola.fivethirtyeight.model.FeedItem
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import org.jsoup.select.Elements
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class SportsDataSourceImpl @Inject constructor(private val apiService: ApiService) :
    SportsDataSource{



        override suspend  fun getSportsFeedList(): MutableList<FeedItem> {

            val listData = mutableListOf<FeedItem>()


            val responseResult = apiService.getFeedItems("abcnews/sportsheadlines")

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

                   /* if (picImage.contains("default") || picImage.isEmpty() || picImage.contains("null")) {
                        picImage = R.drawable.picsart.toString()

                    }*/


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


       override suspend fun getGoogleSports(): MutableList<FeedItem> {

            val listData = mutableListOf<FeedItem>()


            val responseResult =
                apiService.getFeedItems("https://news.google.com/rss/topics/CAAqJggKIiBDQkFTRWdvSUwyMHZNRFp1ZEdvU0FtVnVHZ0pWVXlnQVAB?hl=en-US&gl=US&ceid=US%3Aen")

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

                    /*if (picImage.contains("default") || picImage.isEmpty() || picImage.contains("null")) {
                        picImage = R.drawable.picsart.toString()

                    }*/


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


     override  suspend fun getNySports(): MutableList<FeedItem> {

            val listData = mutableListOf<FeedItem>()


            val responseResult =
                apiService.getFeedItems("https://rss.nytimes.com/services/xml/rss/nyt/sports.xml")

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

                   /* if (picImage.contains("default") || picImage.isEmpty() || picImage.contains("null")) {
                        picImage = R.drawable.picsart.toString()

                    }
*/

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


    override suspend fun getNprSports(): MutableList<FeedItem> {

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

                   /* val imageUrls = itemElements.mapNotNull { item ->
                        val contentEncoded = item.select("encoded").firstOrNull()?.text()
                        val contentHtml = contentEncoded?.let { Jsoup.parse(it) }
                        contentHtml?.select("img")?.firstOrNull()?.attr("src")
                    }*/



                   /* if (picImage != null) {
                        if (picImage.contains("default") || picImage.isEmpty() || picImage.contains("null")) {
                            picImage = R.drawable.picsart.toString()

                        }
                    }*/

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











