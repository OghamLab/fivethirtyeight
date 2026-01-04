package com.ola.fivethirtyeight.utils
import org.jsoup.Jsoup
import org.jsoup.nodes.Element


fun extractGoogleImage(element: Element): String {

    fun clean(url: String?): String? =
        url
            ?.substringBefore("?")
            ?.takeIf { it.startsWith("http") }

    // 1️⃣ media:content (fastest & cleanest when present)
    clean(
        element.getElementsByTag("media:content")
            .firstOrNull()
            ?.attr("url")
    )?.let { return it }

    // 2️⃣ content:encoded HTML (Google AMP)
    element.getElementsByTag("content:encoded")
        .firstOrNull()
        ?.let { encoded ->
            val html = Jsoup.parse(encoded.text())

            clean(
                html.selectFirst("img[src]")?.attr("src")
                    ?: html.selectFirst("img[data-src]")?.attr("data-src")
                    ?: html.selectFirst("amp-img")?.attr("src")
            )?.let { return it }
        }

    // 3️⃣ description HTML (most common)
    element.selectFirst("description")?.text()
        ?.let {
            val html = Jsoup.parse(it)

            clean(
                html.selectFirst("img[src]")?.attr("src")
                    ?: html.selectFirst("img[data-src]")?.attr("data-src")
            )?.let { return it }
        }

    return ""
}


/*fun extractGoogleImage(element: Element): String {

    // 1️⃣ media:content (rare but fastest)
    element.getElementsByTag("media:content")
        .firstOrNull()
        ?.attr("url")
        ?.takeIf { it.isNotBlank() }
        ?.let { return it }

    // 2️⃣ content:encoded HTML
    element.getElementsByTag("content:encoded")
        .firstOrNull()
        ?.let {
            val html = Jsoup.parse(it.text())
            html.selectFirst("img")?.attr("src")
                ?.takeIf { it.isNotBlank() }
                ?.let { return it }
        }

    // 3️⃣ description HTML (most common)
    val descriptionHtml = element.selectFirst("description")?.text()
    if (!descriptionHtml.isNullOrBlank()) {
        val html = Jsoup.parse(descriptionHtml)
        html.selectFirst("img")?.attr("src")
            ?.takeIf { it.isNotBlank() }
            ?.let { return it }
    }

    return ""
}
*/