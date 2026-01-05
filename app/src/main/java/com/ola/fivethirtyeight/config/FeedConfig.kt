package com.ola.fivethirtyeight.config

import com.ola.fivethirtyeight.parser.extractImage
import com.ola.fivethirtyeight.utils.extractGoogleImage
import org.jsoup.nodes.Element

data class FeedConfig(
    val name: String,
    val url: String,
    val imageExtractor: (Element) -> String = ::extractImage,
    val titleTransformer: (String) -> String = { it }
)


 val TOP_STORY_FEEDS = listOf(
    FeedConfig(
        name = "ABC",
        url = "abcnews/topstories",
        imageExtractor = {
            it.getElementsByTag("media:thumbnail")
                .first()?.attr("url") ?: ""
        }
    ),
    FeedConfig(
        name = "Google",
        url = "https://news.google.com/rss/topics/CAAqJggKIiBDQkFTRWdvSUwyMHZNRFZxYUdjU0FtVnVHZ0pWVXlnQVAB?hl=en-US&gl=US&ceid=US%3Aen",
        imageExtractor = { extractGoogleImage(it) },
        titleTransformer = { it.substringBeforeLast("-") }
    ),
    FeedConfig(
        name = "NYT",
        url = "https://rss.nytimes.com/services/xml/rss/nyt/US.xml",
        imageExtractor = {
            it.getElementsByTag("media:content")
                .first()?.attr("url") ?: ""
        }
    ),

    FeedConfig(
        name = "NPR",
        url = "https://feeds.npr.org/1001/rss.xml",
        imageExtractor = {
            it.getElementsByTag("content:encoded")
                .first()
                ?.select("img")
                ?.attr("src")
                ?: ""
        }
    )





)
