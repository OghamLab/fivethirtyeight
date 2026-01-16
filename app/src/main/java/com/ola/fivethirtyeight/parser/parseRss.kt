package com.ola.fivethirtyeight.parser



import com.ola.fivethirtyeight.model.FeedItem
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser
import java.text.SimpleDateFormat
import java.util.Locale

private val RSS_DATE_FORMATS = listOf(
    "EEE, dd MMM yyyy HH:mm:ss Z",
    "EEE, d MMM yyyy HH:mm:ss Z",
    "yyyy-MM-dd'T'HH:mm:ss'Z'",
    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
).map {
    SimpleDateFormat(it, Locale.ENGLISH).apply { isLenient = true }
}

private fun parsePubDate(date: String): Long? {
    for (format in RSS_DATE_FORMATS) {
        try {
            return format.parse(date)?.time
        } catch (_: Exception) {
        }
    }
    return null
}


private fun isValidItem(
    title: String,
    link: String,
    time: Long
): Boolean {
    return title.isNotBlank() &&
            link.startsWith("http") &&
            time > 0
}

private fun normalizeLink(link: String): String =
    link.substringBefore("?").trim()

fun extractImage(element: Element): String {
    return element.getElementsByTag("media:thumbnail").first()?.attr("url")
        ?: element.getElementsByTag("media:content").first()?.attr("url")
        ?: element.select("enclosure[url]").attr("url")
}


fun parseRss(
    xml: String,
    imageExtractor: (Element) -> String = ::extractImage,
    titleTransformer: (String) -> String = { it }
): List<FeedItem> {

    val doc = Jsoup.parse(xml, "", Parser.xmlParser())
    val items = doc.select("item")

    val result = mutableListOf<FeedItem>()

    for (element in items) {
        val rawTitle = element.selectFirst("title")?.text().orEmpty()
        val title = titleTransformer(rawTitle)

        val description = Jsoup
            .parse(element.selectFirst("description")?.text().orEmpty())
            .text()

        val pubDate = element.selectFirst("pubDate")?.text().orEmpty()
        val timeInMil = parsePubDate(pubDate) ?: continue

        val link = normalizeLink(
            element.selectFirst("link")?.text().orEmpty()
        )
        if (!isValidItem(title, link, timeInMil)) continue
        val image = imageExtractor(element)

        result.add(
            FeedItem(
                title = title,
                description = description,
                content = "",
                author = "",
                publishedAt = pubDate,
                imageUrl = image,
                link = link,
                savedDate = "",
                timeInMil = timeInMil
            )
        )
    }

    return result
        .distinctBy { it.link }
        .sortedByDescending { it.timeInMil }
}