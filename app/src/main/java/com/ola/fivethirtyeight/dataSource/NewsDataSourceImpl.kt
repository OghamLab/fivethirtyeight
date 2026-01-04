package com.ola.fivethirtyeight.dataSource

import com.ola.fivethirtyeight.api.ApiService
import com.ola.fivethirtyeight.model.FeedItem
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser
import org.jsoup.select.Elements
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class NewsDataSourceImpl @Inject constructor (private val apiService: ApiService):
    NewsDataSource {


        override suspend fun getFeedList(): MutableList<FeedItem>{

           val listData = mutableListOf<FeedItem>()

           val responseResult = apiService.getFeedItems("https://natesilver.substack.com/feed")

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

                  // System.out.println("links  " + sLink)

                   val responseResult2 = apiService.getFeedItems(sLink)
                   val doc3 = Jsoup.parse(responseResult2.body().toString())

                  var imageUrl: String? = null
                   // 1. <enclosure>
                   val enclosure: Element? = element.selectFirst("enclosure[url]")
                   if (enclosure != null) {
                       imageUrl = enclosure.attr("url")
                   }
                   // 2. <media:content>
                   if (imageUrl == null) {
                       val media: Element? = element.selectFirst("media|content, content")
                       if (media != null && media.hasAttr("url")) {
                           imageUrl = media.attr("url")
                       }
                   }

                    // 3. Inside <content:encoded>
                   if (imageUrl == null) {
                       val content: Element? = element.selectFirst("content|encoded")
                       if (content != null) {
                           val inner = Jsoup.parse(content.text())
                           val img = inner.selectFirst("img")
                           if (img != null) {
                               imageUrl = img.attr("src")
                           }
                       }
                   }





                   listData.add(
                       FeedItem(
                           sTitle,
                           sDesc,
                           "",   //contentText2.toString(),
                           "",  // sAuthor,
                           sDate,
                           imageUrl.toString(),
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







