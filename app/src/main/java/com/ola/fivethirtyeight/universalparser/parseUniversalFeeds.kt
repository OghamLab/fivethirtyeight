@file:Suppress("unused")
package com.ola.fivethirtyeight.universalparser

// NOTE: Assumes you already have:
// data class FeedItem(
//     val title: String,
//     val description: String,
//     val content: String,
//     val author: String,
//     val publishedAt: String,
//     val imageUrl: String,
//     val link: String,
//     val savedDate: String,
//     val timeInMil: Long,
//     val isSavedForLater: Boolean
// )

// ---------------------------------------------------------
// PUBLIC ENTRY POINT
// ---------------------------------------------------------

/*
suspend fun parseUniversalFeeds(urls: List<String>): List<FeedItem> = withContext(Dispatchers.IO) {
    val allItems = mutableListOf<FeedItem>()

    for (url in urls) {
        val itemsForUrl = runCatching {
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 UniversalNewsParser")
                .timeout(15000)
                .ignoreContentType(true)
                .followRedirects(true)
                .get()

            parseSingleDocument(url, doc)
        }.getOrElse { emptyList() }

        allItems += itemsForUrl
    }

    val deduped = dedupeByLink(allItems)
    sortByPublishedDate(deduped)
}
*/






import com.ola.fivethirtyeight.model.FeedItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser
import java.net.URI
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

// ---------------------------------------------------------
// PUBLIC DATA MODELS
// ---------------------------------------------------------



/**
 * Cluster of related stories across sources (Google News style).
 * - fingerprint: content-based key
 * - items: all variants from different sources
 * - best: the chosen representative for UI
 */
data class StoryCluster(
    val fingerprint: String,
    val items: List<FeedItem>,
    val best: FeedItem
)

// ---------------------------------------------------------
// HTTP FALLBACK CONFIG + HELPERS
// ---------------------------------------------------------

// Domains allowed to fall back to HTTP if HTTPS fails
private val httpFallbackWhitelist = setOf(
    // Add real HTTP-only domains here if you ever encounter them
    "example.com",
    "legacyfeed.org"
)

// Try HTTPS first
private fun upgradeToHttps(url: String): String {
    return if (url.startsWith("http://", ignoreCase = true)) {
        url.replaceFirst("http://", "https://")
    } else url
}

// Extract domain
private fun domainOf(url: String): String {
    return try {
        URI(url).host?.removePrefix("www.") ?: ""
    } catch (_: Exception) {
        ""
    }
}

private fun logHttpFallback(originalUrl: String, httpsUrl: String, reason: String) {
    val domain = domainOf(originalUrl)
    println("HTTP-FALLBACK: domain=$domain | https=$httpsUrl | reason=$reason")
}

/**
 * Fetch a Document with:
 * - HTTPS upgrade
 * - HTTP fallback for whitelisted domains
 * - Logging for failures and fallbacks
 */
private fun fetchDocumentWithHttpsFallback(url: String, timeoutMs: Int): Document {
    val httpsUrl = upgradeToHttps(url)
    val domain = domainOf(url)

    // 1. Try HTTPS first
    try {
        return Jsoup.connect(httpsUrl)
            .userAgent("Mozilla/5.0 UniversalNewsParser")
            .timeout(timeoutMs)
            .ignoreContentType(true)
            .followRedirects(true)
            .get()
    } catch (e: Exception) {
        logHttpFallback(url, httpsUrl, "HTTPS failed: ${e.message}")
    }

    // 2. If HTTPS failed, only allow HTTP fallback if whitelisted
    if (domain in httpFallbackWhitelist) {
        val httpUrl = httpsUrl.replaceFirst("https://", "http://")
        try {
            logHttpFallback(url, httpsUrl, "Falling back to HTTP (whitelisted)")
            return Jsoup.connect(httpUrl)
                .userAgent("Mozilla/5.0 UniversalNewsParser")
                .timeout(timeoutMs)
                .ignoreContentType(true)
                .followRedirects(true)
                .get()
        } catch (e: Exception) {
            logHttpFallback(url, httpsUrl, "HTTP fallback failed: ${e.message}")
            throw e
        }
    } else {
        logHttpFallback(url, httpsUrl, "Domain not whitelisted — blocking HTTP")
        throw IllegalStateException("HTTP not allowed for domain: $domain")
    }
}

// ---------------------------------------------------------
// PUBLIC API
// ---------------------------------------------------------

/**
 * Universal feed parser:
 * - Parallel fetch
 * - HTTPS upgrade + HTTP fallback
 * - RSS / Atom / Google News / HTML
 * - Canonicalization during parsing
 * - Link + content dedupe
 * - Confidence + source + freshness scoring
 */
suspend fun parseUniversalFeeds(urls: List<String>): List<FeedItem> = withContext(Dispatchers.IO) {

    // -----------------------------
    // CONFIG
    // -----------------------------
    val maxConcurrency = 8
    val maxRetries = 3
    val perFeedTimeoutMs = 15_000L

    // -----------------------------
    // CONCURRENCY LIMITER
    // -----------------------------
    val semaphore = Semaphore(maxConcurrency)

    // -----------------------------
    // PARALLEL FETCH + PARSE
    // -----------------------------
    val deferreds = urls.map { url ->
        async {
            semaphore.acquire()
            try {
                retry(maxRetries) { _ ->
                    withTimeout(perFeedTimeoutMs) {
                        val doc = fetchDocumentWithHttpsFallback(url, perFeedTimeoutMs.toInt())
                        parseSingleDocument(url, doc)
                    }
                }
            } catch (_: Exception) {
                emptyList<FeedItem>()
            } finally {
                semaphore.release()
            }
        }
    }

    val allItems = deferreds.awaitAll().flatten()

    // -----------------------------
    // DEDUPE + CLUSTER + RANK
    // -----------------------------
    val linkDeduped = dedupeByLink(allItems)
    val contentDeduped = dedupeByContent(linkDeduped)
    val sorted = sortByFinalScore(contentDeduped)

    sorted
}

/**
 * Optional: cluster stories for a Google News style UI.
 * You can call this on the result of parseUniversalFeeds().
 */
fun clusterStories(items: List<FeedItem>): List<StoryCluster> {
    val clusters = LinkedHashMap<String, MutableList<FeedItem>>()

    for (item in items) {
        val key = contentFingerprint(item)
        clusters.getOrPut(key) { mutableListOf() }.add(item)
    }

    return clusters.map { (fp, list) ->
        val best = list.maxByOrNull { finalScore(it) }!!
        StoryCluster(fp, list, best)
    }
}

// ---------------------------------------------------------
// CORE DISPATCH
// ---------------------------------------------------------

private fun parseSingleDocument(url: String, doc: Document): List<FeedItem> {
    val rootTag = doc.child(0)?.tagName()?.lowercase(Locale.ROOT) ?: ""

    // RSS
    if (doc.selectFirst("rss, channel, item") != null) {
        return extractRss(doc)
    }

    // Atom
    if (doc.selectFirst("feed > entry") != null || rootTag == "feed") {
        return extractAtom(doc)
    }

    // Google News RSS (uses g:originalLink)
    if (doc.selectFirst("item g\\:originalLink") != null) {
        return extractGoogleNews(doc)
    }

    // Fallback: HTML full article extraction
    val article = extractHtml(url, doc)
    return if (article.title.isNotBlank()) listOf(article) else emptyList()
}

// ---------------------------------------------------------
// RSS
// ---------------------------------------------------------

private fun extractRss(doc: Document): List<FeedItem> {
    val items = mutableListOf<FeedItem>()

    doc.select("item").forEach { itemEl ->
        val rawLink = itemEl.selectFirst("link")?.text()?.trim().orEmpty()
        val link = canonicalizeLink(rawLink)
        if (link.isBlank()) return@forEach

        val title = normalizeTitle(
            itemEl.selectFirst("title")?.text().orEmpty()
        )

        val descriptionHtml =
            itemEl.selectFirst("description")?.text().orEmpty()
                .ifBlank { itemEl.selectFirst("content\\:encoded")?.text().orEmpty() }

        val description = normalizeDescription(stripHtml(descriptionHtml))

        val author =
            itemEl.selectFirst("author")?.text().orEmpty()
                .ifBlank { itemEl.selectFirst("dc\\:creator")?.text().orEmpty() }

        val pubRaw =
            itemEl.selectFirst("pubDate")?.text().orEmpty()
                .ifBlank { itemEl.selectFirst("dc\\:date")?.text().orEmpty() }

        val publishedAt = pubRaw.trim()
        val timeInMil = parseDateToMillis(publishedAt)

        val enclosureImage =
            itemEl.selectFirst("enclosure[url]")?.attr("url")
                ?: itemEl.selectFirst("media\\:content[url]")?.attr("url")

        val contentHtml =
            itemEl.selectFirst("content\\:encoded")?.text().orEmpty()
                .ifBlank { descriptionHtml }

        // Try to find image inside HTML if no enclosure
        val docContent = runCatching { Jsoup.parse(contentHtml) }.getOrNull()
        val bodyImage = docContent?.selectFirst("img[src]")?.absUrl("src").orEmpty()

        val imageUrl = normalizeImageUrl(
            enclosureImage ?: bodyImage
        )

        items += FeedItem(
            title = title,
            description = description,
            content = sanitizeContentHtml(contentHtml),
            author = author,
            publishedAt = publishedAt,
            imageUrl = imageUrl,
            link = link,
            savedDate = "",
            timeInMil = timeInMil,
            isSavedForLater = false
        )
    }

    return items
}

// ---------------------------------------------------------
// ATOM
// ---------------------------------------------------------

private fun extractAtom(doc: Document): List<FeedItem> {
    val items = mutableListOf<FeedItem>()

    doc.select("entry").forEach { entryEl ->
        val linkEl = entryEl.selectFirst("link[rel=alternate], link[href]")
        val rawLink = linkEl?.attr("href")?.trim().orEmpty()
        val link = canonicalizeLink(rawLink)
        if (link.isBlank()) return@forEach

        val title = normalizeTitle(
            entryEl.selectFirst("title")?.text().orEmpty()
        )

        val summaryHtml = entryEl.selectFirst("summary")?.text().orEmpty()
        val contentHtml =
            entryEl.selectFirst("content")?.text().orEmpty()
                .ifBlank { summaryHtml }

        val description = normalizeDescription(
            stripHtml(summaryHtml.ifBlank { contentHtml })
        )

        val author =
            entryEl.selectFirst("author > name")?.text().orEmpty()
                .ifBlank { entryEl.selectFirst("author")?.text().orEmpty() }

        val pubRaw =
            entryEl.selectFirst("updated")?.text().orEmpty()
                .ifBlank { entryEl.selectFirst("published")?.text().orEmpty() }

        val publishedAt = pubRaw.trim()
        val timeInMil = parseDateToMillis(publishedAt)

        val imageUrl = normalizeImageUrl(
            entryEl.selectFirst("media\\:content[url]")?.attr("url")
                ?: entryEl.selectFirst("link[rel=enclosure][type^=image]")?.attr("href")
        )

        items += FeedItem(
            title = title,
            description = description,
            content = sanitizeContentHtml(contentHtml),
            author = author,
            publishedAt = publishedAt,
            imageUrl = imageUrl,
            link = link,
            savedDate = "",
            timeInMil = timeInMil,
            isSavedForLater = false
        )
    }

    return items
}

// ---------------------------------------------------------
// GOOGLE NEWS RSS
// ---------------------------------------------------------

private fun extractGoogleNews(doc: Document): List<FeedItem> {
    val items = mutableListOf<FeedItem>()

    doc.select("item").forEach { itemEl ->
        val rawLink = itemEl.selectFirst("g\\:originalLink")?.text()?.trim().orEmpty()
        val link = canonicalizeLink(rawLink)
        if (link.isBlank()) return@forEach

        val title = normalizeTitle(
            itemEl.selectFirst("title")?.text().orEmpty()
        )

        val descriptionHtml =
            itemEl.selectFirst("description")?.text().orEmpty()
        val description = normalizeDescription(stripHtml(descriptionHtml))

        val source = itemEl.selectFirst("source")?.text().orEmpty()

        val pubRaw = itemEl.selectFirst("pubDate")?.text().orEmpty()
        val publishedAt = pubRaw.trim()
        val timeInMil = parseDateToMillis(publishedAt)

        val imageUrl = normalizeImageUrl(
            runCatching {
                val snippetDoc = Jsoup.parse(descriptionHtml)
                snippetDoc.selectFirst("img[src]")?.absUrl("src")
            }.getOrNull()
        )

        items += FeedItem(
            title = title,
            description = description,
            content = sanitizeContentHtml(descriptionHtml),
            author = source,
            publishedAt = publishedAt,
            imageUrl = imageUrl,
            link = link,
            savedDate = "",
            timeInMil = timeInMil,
            isSavedForLater = false
        )
    }

    return items
}

// ---------------------------------------------------------
// FULL HTML ARTICLE EXTRACTION (OPTION C)
// ---------------------------------------------------------

private fun extractHtml(url: String, doc: Document): FeedItem {
    val canonicalUrl = canonicalizeLink(url)

    // ---------- OpenGraph ----------
    val ogTitle = doc.selectFirst("meta[property=og:title]")?.attr("content")
    val ogDesc = doc.selectFirst("meta[property=og:description]")?.attr("content")
    val ogImage = doc.selectFirst("meta[property=og:image]")?.attr("content")
    val ogPub = doc.selectFirst("meta[property=article:published_time]")?.attr("content")
    val ogAuthor = doc.selectFirst("meta[property=article:author]")?.attr("content")

    // ---------- Twitter Card ----------
    val twTitle = doc.selectFirst("meta[name=twitter:title]")?.attr("content")
    val twDesc = doc.selectFirst("meta[name=twitter:description]")?.attr("content")
    val twImage =
        doc.selectFirst("meta[name=twitter:image], meta[name=twitter:image:src]")?.attr("content")

    // ---------- JSON-LD ----------
    var ldTitle: String? = null
    var ldDesc: String? = null
    var ldImage: String? = null
    var ldPub: String? = null
    var ldAuthor: String? = null
    var ldBody: String? = null

    doc.select("script[type=application/ld+json]").forEach { script ->
        try {
            val text = script.html()
            if (text.isBlank()) return@forEach

            val jsonAny = JSONObject(text)
            val candidates = mutableListOf<JSONObject>()

            if (jsonAny.has("@type")) {
                candidates += jsonAny
            } else if (jsonAny.has("@graph")) {
                val graph = jsonAny.getJSONArray("@graph")
                for (i in 0 until graph.length()) {
                    val item = graph.optJSONObject(i) ?: continue
                    candidates += item
                }
            }

            candidates.forEach { json ->
                val type = json.optString("@type").lowercase(Locale.ROOT)
                if ("article" in type || "newsarticle" in type || "blogposting" in type) {
                    ldTitle = ldTitle ?: json.optString("headline", null)
                    ldDesc = ldDesc ?: json.optString("description", null)
                    ldPub = ldPub ?: json.optString("datePublished", null)

                    if (ldAuthor == null) {
                        ldAuthor =
                            json.optJSONObject("author")?.optString("name", null)
                                ?: json.optJSONArray("author")?.optJSONObject(0)
                                    ?.optString("name", null)
                    }

                    if (ldImage == null && json.has("image")) {
                        when (val img = json.get("image")) {
                            is String -> ldImage = img
                            is JSONArray -> ldImage = img.optString(0, null)
                            is JSONObject -> ldImage = img.optString("url", null)
                        }
                    }

                    ldBody = ldBody ?: json.optString("articleBody", null)
                }
            }
        } catch (_: Exception) {
        }
    }

    // ---------- Headline ----------
    val h1Title = doc.selectFirst("h1")?.text()

    // ---------- Article Element ----------
    val articleEl: Element? =
        doc.selectFirst("article")
            ?: doc.selectFirst("main article")
            ?: doc.selectFirst("div[itemprop=articleBody]")
            ?: doc.selectFirst("div[class*=article-body]")
            ?: doc.selectFirst("div[class*=story-body]")
            ?: doc.selectFirst("div[id*=article-body]")
            ?: doc.selectFirst("section[class*=article]")

    // ---------- Article Body ----------
    val articleBody = when {
        !ldBody.isNullOrBlank() -> ldBody!!
        articleEl != null -> articleEl.select("p").joinToString("\n") { it.text() }
        else -> doc.select("article p, main p, p").joinToString("\n") { it.text() }
    }

    // ---------- Hero Image ----------
    val heroImage =
        ogImage
            ?: twImage
            ?: ldImage
            ?: articleEl?.selectFirst("img[src]")?.absUrl("src")
            ?: doc.selectFirst("img[class*=hero][src]")?.absUrl("src")
            ?: doc.selectFirst("img[class*=featured][src]")?.absUrl("src")
            ?: doc.selectFirst("meta[property=og:image:secure_url]")?.attr("content")
            ?: doc.selectFirst("img[src]")?.absUrl("src")

    // ---------- Description ----------
    val description =
        ogDesc
            ?: twDesc
            ?: ldDesc
            ?: articleEl?.selectFirst("p")?.text()
            ?: doc.selectFirst("article p, main p, p")?.text()
            ?: ""

    // ---------- Published ----------
    val publishedAt =
        ogPub
            ?: ldPub
            ?: doc.selectFirst("time[datetime]")?.attr("datetime")
            ?: doc.selectFirst("meta[name=date], meta[property=article:modified_time]")
                ?.attr("content")
            ?: ""

    // ---------- Author ----------
    val author =
        ogAuthor
            ?: ldAuthor
            ?: doc.selectFirst("meta[name=author]")?.attr("content")
            ?: doc.selectFirst("[class*=author], [itemprop=author]")?.text()
            ?: ""

    // ---------- Final Title ----------
    val title =
        normalizeTitle(
            ogTitle
                ?: twTitle
                ?: ldTitle
                ?: h1Title
                ?: doc.title()
        )

    val cleanedDescription = normalizeDescription(description)
    val contentHtml = sanitizeContentHtml(articleBody)
    val imageUrl = normalizeImageUrl(heroImage)
    val timeInMil = parseDateToMillis(publishedAt)

    return FeedItem(
        title = title,
        description = cleanedDescription,
        content = contentHtml,
        author = author,
        publishedAt = publishedAt,
        imageUrl = imageUrl,
        link = canonicalUrl,
        savedDate = "",
        timeInMil = timeInMil,
        isSavedForLater = false
    )
}

// ---------------------------------------------------------
// NORMALIZATION & SANITIZATION
// ---------------------------------------------------------

private fun normalizeTitle(raw: String): String {
    if (raw.isBlank()) return ""
    val decoded = Parser.unescapeEntities(raw, false)
    return cleanWhitespace(decoded)
}

private fun normalizeDescription(raw: String): String {
    if (raw.isBlank()) return ""
    val decoded = Parser.unescapeEntities(raw, false)
    val stripped = stripHtml(decoded)
    return cleanWhitespace(stripped)
}

private fun normalizeImageUrl(raw: String?): String {
    if (raw.isNullOrBlank()) return ""

    var url = raw.trim()

    // Remove tracking parameters
    url = url.substringBefore("?")

    // Fix schema-less URLs: //cdn.site.com/img.jpg
    if (url.startsWith("//")) {
        url = "https:$url"
    }

    // Reject base64 images
    if (url.startsWith("data:image")) return ""

    // Reject tiny placeholder images
    if (url.endsWith("1x1.png") || url.endsWith("1x1.jpg")) return ""

    // Google AMP / proxy cleanup
    if (url.contains("googleusercontent.com")) {
        url = url.substringBefore("=w") // remove resizing params
    }

    // Normalize whitespace
    return url.replace("\\s+".toRegex(), "")
}

private fun sanitizeContentHtml(raw: String): String {
    // Currently: just trim. You can extend to whitelist tags if you want.
    return raw.trim()
}

private fun stripHtml(raw: String): String {
    if (raw.isBlank()) return ""
    return Jsoup.parse(raw).text()
}

private fun cleanWhitespace(raw: String): String {
    return raw.replace("\\s+".toRegex(), " ").trim()
}

// ---------------------------------------------------------
// DATE, DEDUPE, SORT, RETRY
// ---------------------------------------------------------

private fun parseDateToMillis(dateStr: String): Long {
    val trimmed = dateStr.trim()
    if (trimmed.isBlank()) return System.currentTimeMillis()

    val patterns = listOf(
        "EEE, dd MMM yyyy HH:mm:ss Z",          // RSS pubDate
        "EEE, dd MMM yyyy HH:mm:ss zzz",        // Alternative RSS
        "yyyy-MM-dd'T'HH:mm:ss'Z'",             // ISO Zulu
        "yyyy-MM-dd'T'HH:mm:ssXXX",             // ISO with timezone offset
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",         // ISO with millis (Zulu)
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",         // ISO with millis + offset
        "yyyy-MM-dd'T'HH:mm:ssZ",               // ISO without colon in offset
        "yyyy-MM-dd'T'HH:mm:ss.SSSZ",           // ISO millis without colon
        "yyyy-MM-dd'T'HH:mm:ss",                // ISO no timezone
        "yyyy-MM-dd HH:mm:ss",                  // Common HTML meta format
        "yyyy-MM-dd"                            // Date only
    )

    for (pattern in patterns) {
        try {
            val sdf = SimpleDateFormat(pattern, Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = sdf.parse(trimmed)
            if (date != null) return date.time
        } catch (_: Exception) {
        }
    }

    return System.currentTimeMillis()
}

private fun dedupeByLink(items: List<FeedItem>): List<FeedItem> {
    val seen = LinkedHashMap<String, FeedItem>()
    for (item in items) {
        val key = canonicalizeLink(item.link.trim())
        if (key.isBlank()) continue
        if (!seen.containsKey(key)) {
            seen[key] = item
        }
    }
    return seen.values.toList()
}

private fun dedupeByContent(items: List<FeedItem>): List<FeedItem> {
    val seen = LinkedHashMap<String, FeedItem>()
    for (item in items) {
        val key = contentFingerprint(item)
        if (!seen.containsKey(key)) {
            seen[key] = item
        }
    }
    return seen.values.toList()
}

private fun sortByFinalScore(items: List<FeedItem>): List<FeedItem> {
    return items.sortedByDescending { finalScore(it) }
}

private suspend fun <T> retry(
    times: Int,
    block: suspend (attempt: Int) -> T
): T {
    var lastError: Throwable? = null

    repeat(times) { attempt ->
        try {
            return block(attempt + 1)
        } catch (e: Throwable) {
            lastError = e
            val delayMs = 300L * (attempt + 1)
            delay(delayMs)
        }
    }

    throw lastError ?: IllegalStateException("Unknown error in retry()")
}

// ---------------------------------------------------------
// AGGREGATOR DETECTION
// ---------------------------------------------------------

private val AGGREGATOR_DOMAINS = setOf(
    "news.google.com",
    "news.yahoo.com",
    "rss.news.yahoo.com",
    "msn.com",
    "flipboard.com",
    "apple.news"
)

private fun FeedItem.sourceDomain(): String =
    extractDomain(this.link)

private fun FeedItem.isAggregator(): Boolean =
    sourceDomain() in AGGREGATOR_DOMAINS

private fun FeedItem.isPrimarySource(): Boolean {
    val linkDomain = extractDomain(this.link)
    return !isAggregator() || linkDomain !in AGGREGATOR_DOMAINS
}

// ---------------------------------------------------------
// CONFIDENCE SCORING + SOURCE RANK + FINAL SCORE
// ---------------------------------------------------------

fun computeConfidenceScore(item: FeedItem): Int {
    var score = 0

    // Content quality
    if (item.title.length > 15) score += 10
    if (item.description.length > 50) score += 10
    if (item.content.length > 300) score += 20
    if (item.content.length > 800) score += 10

    // Metadata completeness
    if (item.author.isNotBlank()) score += 10
    if (item.imageUrl.isNotBlank()) score += 10
    if (item.timeInMil > 0) score += 10

    // Source quality
    if (item.isPrimarySource()) score += 20

    return score.coerceIn(0, 100)
}

private val SOURCE_RANK = mapOf(
    "reuters.com" to 100,
    "apnews.com" to 95,
    "bbc.com" to 90,
    "nytimes.com" to 85,
    "washingtonpost.com" to 80,
    "npr.org" to 75,
    "theguardian.com" to 70,
    "cnn.com" to 60,
    "foxnews.com" to 50,
    "unknown" to 10
)

private fun sourceRank(item: FeedItem): Int {
    val domain = extractDomain(item.link)
    return SOURCE_RANK[domain] ?: SOURCE_RANK["unknown"]!!
}

private fun finalScore(item: FeedItem): Int {
    val now = System.currentTimeMillis()
    val freshness = now - item.timeInMil
    val freshnessScore = when {
        freshness < 1 * 60 * 60 * 1000 -> 30   // < 1 hour
        freshness < 6 * 60 * 60 * 1000 -> 20   // < 6 hours
        freshness < 24 * 60 * 60 * 1000 -> 10  // < 24 hours
        else -> 0
    }

    return computeConfidenceScore(item) +
            sourceRank(item) +
            freshnessScore
}

// ---------------------------------------------------------
// CANONICAL URL NORMALIZATION
// ---------------------------------------------------------

fun canonicalizeLink(url: String): String {
    return try {
        val uri = URI(url)
        val cleanQuery = uri.query
            ?.split("&")
            ?.filterNot {
                it.startsWith("utm_") ||
                        it.startsWith("fbclid") ||
                        it.startsWith("gclid")
            }
            ?.joinToString("&")

        URI(uri.scheme, uri.authority, uri.path, cleanQuery, null).toString()
    } catch (_: Exception) {
        url.trim()
    }
}

// ---------------------------------------------------------
// CONTENT FINGERPRINT (CROSS-SOURCE)
// ---------------------------------------------------------

private fun contentFingerprint(item: FeedItem): String {
    val base = buildString {
        append(item.title.lowercase())
        append("|")
        append(item.description.take(200).lowercase())
    }

    return MessageDigest.getInstance("SHA-256")
        .digest(base.toByteArray())
        .joinToString("") { "%02x".format(it) }
}

// ---------------------------------------------------------
// NOTIFICATION-SAFE DEDUPE (OPTIONAL)
// ---------------------------------------------------------

fun dedupeForNotifications(items: List<FeedItem>): List<FeedItem> {
    val bestByFingerprint = LinkedHashMap<String, FeedItem>()

    for (item in items) {
        val key = contentFingerprint(item)
        val existing = bestByFingerprint[key]

        if (
            existing == null ||
            computeConfidenceScore(item) > computeConfidenceScore(existing)
        ) {
            bestByFingerprint[key] = item
        }
    }

    return bestByFingerprint.values.toList()
}

// ---------------------------------------------------------
// DOMAIN HELPER
// ---------------------------------------------------------

private fun extractDomain(url: String): String =
    try {
        URI(url).host?.removePrefix("www.") ?: ""
    } catch (_: Exception) {
        ""
    }


// ---------------------------------------------------------
// DATA MODEL — YOUR EXACT FeedItem
// ---------------------------------------------------------


// ---------------------------------------------------------
// PUBLIC API
// ---------------------------------------------------------

/**
 * Universal feed parser entry point.
 *
 * - Fetches all URLs in parallel with:
 *   - concurrency limit
 *   - per-feed timeout
 *   - retry logic
 * - Parses RSS/Atom/HTML
 * - Uses per-site overrides + readability scoring for HTML.
 */


/*
suspend fun parseUniversalFeeds(urls: List<String>): List<FeedItem> = withContext(Dispatchers.IO) {

    val maxConcurrency = 8
    val maxRetries = 3
    val perFeedTimeoutMs = 15_000L

    val semaphore = Semaphore(maxConcurrency)

    val deferreds = urls.map { url ->
        async {
            semaphore.acquire()
            try {
                retry(maxRetries) {
                    withTimeout(perFeedTimeoutMs) {
                        val doc = Jsoup.connect(url)
                            .userAgent("Mozilla/5.0 (UniversalNewsParser)")
                            .timeout(perFeedTimeoutMs.toInt())
                            .ignoreContentType(true)
                            .followRedirects(true)
                            .get()

                        parseSingleDocument(url, doc)
                    }
                }
            } catch (_: Exception) {
                emptyList<FeedItem>()
            } finally {
                semaphore.release()
            }
        }
    }

    val allItems = deferreds.awaitAll().flatten()
    val deduped = dedupeByLink(allItems)
    sortByPublishedDate(deduped)
}

// ---------------------------------------------------------
// CORE DISPATCH: RSS/ATOM vs HTML
// ---------------------------------------------------------

private fun parseSingleDocument(url: String, doc: Document): List<FeedItem> {
    return when {
        doc.selectFirst("rss, channel, feed") != null -> parseFeedDocument(url, doc)
        else -> listOf(extractHtmlWithOverrides(url, doc))
    }
}

// ---------------------------------------------------------
// FEED (RSS / ATOM) PARSING
// ---------------------------------------------------------

private fun parseFeedDocument(url: String, doc: Document): List<FeedItem> {
    val items = mutableListOf<FeedItem>()
    val source = inferSourceNameFromFeed(url, doc)

    // -------- RSS <item> --------
    val rssItems = doc.select("item")
    if (rssItems.isNotEmpty()) {
        for (item in rssItems) {
            val titleRaw = item.selectFirst("title")?.text().orEmpty()
            val link = item.selectFirst("link")?.text().orEmpty()
            val descRaw = item.selectFirst("description")?.text().orEmpty()
            val contentRaw = item.selectFirst("encoded")?.text()
                ?: item.selectFirst("content:encoded")?.text()
                ?: descRaw

            val pubRaw = item.selectFirst("pubDate")?.text()
                ?: item.selectFirst("dc:date")?.text()
                ?: ""

            val authorRaw = item.selectFirst("author")?.text()
                ?: item.selectFirst("dc:creator")?.text()
                ?: source

            val imageRaw =
                item.select("media\\:content, media\\:thumbnail, enclosure[url]")
                    .firstOrNull()?.attr("url")
                    ?: ""

            val title = normalizeTitle(titleRaw)
            val description = normalizeDescription(descRaw)
            val imageUrl = normalizeImageUrl(imageRaw)
            val sanitizedContent = sanitizeContentHtml(
                if (contentRaw.isNotBlank()) contentRaw else descRaw
            )
            val timeInMil = parseDateToMillis(pubRaw)

            items += FeedItem(
                title = title,
                description = description,
                content = sanitizedContent,
                author = authorRaw.ifBlank { source },
                publishedAt = pubRaw.trim(),
                imageUrl = imageUrl,
                link = link,
                savedDate = "",
                timeInMil = timeInMil,
                isSavedForLater = false
            )
        }
        return items
    }

    // -------- Atom <entry> --------
    val atomEntries = doc.select("entry")
    if (atomEntries.isNotEmpty()) {
        for (entry in atomEntries) {
            val titleRaw = entry.selectFirst("title")?.text().orEmpty()
            val link = entry.selectFirst("link[rel=alternate]")?.attr("href")
                ?: entry.selectFirst("link")?.attr("href").orEmpty()
            val descRaw = entry.selectFirst("summary")?.text().orEmpty()
            val contentRaw = entry.selectFirst("content")?.text()
                ?: descRaw

            val pubRaw = entry.selectFirst("updated")?.text()
                ?: entry.selectFirst("published")?.text()
                ?: ""

            val authorRaw = entry.selectFirst("author > name")?.text().orEmpty()

            val imageRaw =
                entry.select("media\\:content, media\\:thumbnail").firstOrNull()?.attr("url")
                    ?: ""

            val title = normalizeTitle(titleRaw)
            val description = normalizeDescription(descRaw)
            val imageUrl = normalizeImageUrl(imageRaw)
            val sanitizedContent = sanitizeContentHtml(
                if (contentRaw.isNotBlank()) contentRaw else descRaw
            )
            val timeInMil = parseDateToMillis(pubRaw)

            items += FeedItem(
                title = title,
                description = description,
                content = sanitizedContent,
                author = authorRaw.ifBlank { source },
                publishedAt = pubRaw.trim(),
                imageUrl = imageUrl,
                link = link,
                savedDate = "",
                timeInMil = timeInMil,
                isSavedForLater = false
            )
        }
    }

    return items
}

// ---------------------------------------------------------
// HTML EXTRACTOR WITH PER-SITE OVERRIDES + READABILITY
// content = sanitized HTML article body (or fallback summary)
// ---------------------------------------------------------

private fun extractHtmlWithOverrides(url: String, doc: Document): FeedItem {
    val domain = extractDomain(url)
    val source = inferSourceNameFromDomain(domain)

    return when (domain) {
        "nytimes.com" -> extractNYT(url, doc, source)
        "bbc.com" -> extractBBC(url, doc, source)
        "cnn.com" -> extractCNN(url, doc, source)
        "reuters.com" -> extractReuters(url, doc, source)
        "apnews.com" -> extractAP(url, doc, source)
        else -> extractHtmlGeneric(url, doc, source)
    }
}

private fun extractHtmlGeneric(
    url: String,
    doc: Document,
    source: String
): FeedItem {

    val titleRaw =
        doc.selectFirst("meta[property=og:title]")?.attr("content")
            ?: doc.selectFirst("title")?.text()
            ?: ""

    val imageRaw =
        doc.selectFirst("meta[property=og:image]")?.attr("content")
            ?: doc.selectFirst("img[src]")?.absUrl("src")
            ?: ""

    val descRaw =
        doc.selectFirst("meta[name=description]")?.attr("content")
            ?: ""

    val authorRaw =
        doc.selectFirst("meta[name=author]")?.attr("content")
            ?: doc.selectFirst("[itemprop=author]")?.text()
            ?: source

    val pubRaw =
        doc.selectFirst("meta[property=article:published_time]")?.attr("content")
            ?: doc.selectFirst("meta[name=date]")?.attr("content")
            ?: doc.selectFirst("time[datetime]")?.attr("datetime")
            ?: doc.selectFirst("meta[itemprop=datePublished]")?.attr("content")
            ?: doc.selectFirst("meta[name=pubdate]")?.attr("content")
            ?: ""

    val bestElement = findBestContentElement(doc)
    val rawHtml = bestElement?.html().orEmpty()

    val sanitizedContent = sanitizeContentHtml(
        rawHtml.ifBlank { descRaw }
    )

    val title = normalizeTitle(titleRaw)
    val description = normalizeDescription(
        if (descRaw.isNotBlank()) descRaw else stripHtml(sanitizedContent).take(200)
    )
    val imageUrl = normalizeImageUrl(imageRaw)
    val timeInMil = parseDateToMillis(pubRaw)

    return FeedItem(
        title = title,
        description = description,
        content = sanitizedContent,
        author = authorRaw.ifBlank { source },
        publishedAt = pubRaw.trim(),
        imageUrl = imageUrl,
        link = url,
        savedDate = "",
        timeInMil = timeInMil,
        isSavedForLater = false
    )
}

// ---------------------------------------------------------
// EXAMPLE PER-SITE OVERRIDES (NYT, BBC, CNN, Reuters, AP)
// They all ultimately return your FeedItem.
// ---------------------------------------------------------

private fun extractNYT(
    url: String,
    doc: Document,
    source: String
): FeedItem {
    val titleRaw = doc.selectFirst("h1")?.text()
        ?: doc.selectFirst("meta[property=og:title]")?.attr("content")
        ?: ""
    val imageRaw = doc.selectFirst("figure img")?.absUrl("src")
        ?: doc.selectFirst("meta[property=og:image]")?.attr("content")
        ?: ""
    val authorRaw = doc.selectFirst("[itemprop=author]")?.text()
        ?: doc.selectFirst("meta[name=byl]")?.attr("content")
        ?: source
    val pubRaw = doc.selectFirst("time")?.attr("datetime")
        ?: doc.selectFirst("meta[name=ptime]")?.attr("content")
        ?: ""

    val articleRoot = doc.selectFirst("section[name=articleBody]")
        ?: doc.selectFirst("article")

    val rawHtml = articleRoot?.html().orEmpty()
    val sanitizedContent = sanitizeContentHtml(rawHtml)

    val title = normalizeTitle(titleRaw)
    val description = normalizeDescription(stripHtml(sanitizedContent).take(200))
    val imageUrl = normalizeImageUrl(imageRaw)
    val timeInMil = parseDateToMillis(pubRaw)

    return FeedItem(
        title = title,
        description = description,
        content = sanitizedContent,
        author = authorRaw.ifBlank { source },
        publishedAt = pubRaw.trim(),
        imageUrl = imageUrl,
        link = url,
        savedDate = "",
        timeInMil = timeInMil,
        isSavedForLater = false
    )
}

private fun extractBBC(
    url: String,
    doc: Document,
    source: String
): FeedItem {
    val titleRaw = doc.selectFirst("h1")?.text()
        ?: doc.selectFirst("meta[property=og:title]")?.attr("content")
        ?: ""
    val imageRaw = doc.selectFirst("figure img")?.absUrl("src")
        ?: doc.selectFirst("meta[property=og:image]")?.attr("content")
        ?: ""
    val authorRaw = doc.selectFirst("[data-component=byline]")?.text()
        ?: source
    val pubRaw = doc.selectFirst("time")?.attr("datetime")
        ?: doc.selectFirst("meta[property=article:published_time]")?.attr("content")
        ?: ""

    val articleRoot = doc.selectFirst("article")
        ?: doc.selectFirst("[data-component=main]")

    val rawHtml = articleRoot?.html().orEmpty()
    val sanitizedContent = sanitizeContentHtml(rawHtml)

    val title = normalizeTitle(titleRaw)
    val description = normalizeDescription(stripHtml(sanitizedContent).take(200))
    val imageUrl = normalizeImageUrl(imageRaw)
    val timeInMil = parseDateToMillis(pubRaw)

    return FeedItem(
        title = title,
        description = description,
        content = sanitizedContent,
        author = authorRaw.ifBlank { source },
        publishedAt = pubRaw.trim(),
        imageUrl = imageUrl,
        link = url,
        savedDate = "",
        timeInMil = timeInMil,
        isSavedForLater = false
    )
}

private fun extractCNN(
    url: String,
    doc: Document,
    source: String
): FeedItem {
    val titleRaw = doc.selectFirst("h1")?.text()
        ?: doc.selectFirst("meta[property=og:title]")?.attr("content")
        ?: ""
    val imageRaw = doc.selectFirst("meta[property=og:image]")?.attr("content")
        ?: doc.selectFirst("img[src]")?.absUrl("src")
        ?: ""
    val authorRaw = doc.selectFirst("[data-editable=byline]")?.text()
        ?: source
    val pubRaw = doc.selectFirst("meta[name=pubdate]")?.attr("content")
        ?: doc.selectFirst("time")?.attr("datetime")
        ?: ""

    val articleRoot = doc.selectFirst("article")
        ?: doc.selectFirst("[data-zone-label=Body Text]")

    val rawHtml = articleRoot?.html().orEmpty()
    val sanitizedContent = sanitizeContentHtml(rawHtml)

    val title = normalizeTitle(titleRaw)
    val description = normalizeDescription(stripHtml(sanitizedContent).take(200))
    val imageUrl = normalizeImageUrl(imageRaw)
    val timeInMil = parseDateToMillis(pubRaw)

    return FeedItem(
        title = title,
        description = description,
        content = sanitizedContent,
        author = authorRaw.ifBlank { source },
        publishedAt = pubRaw.trim(),
        imageUrl = imageUrl,
        link = url,
        savedDate = "",
        timeInMil = timeInMil,
        isSavedForLater = false
    )
}

private fun extractReuters(
    url: String,
    doc: Document,
    source: String
): FeedItem {
    val titleRaw = doc.selectFirst("h1")?.text()
        ?: doc.selectFirst("meta[property=og:title]")?.attr("content")
        ?: ""
    val imageRaw = doc.selectFirst("meta[property=og:image]")?.attr("content")
        ?: ""
    val authorRaw = doc.selectFirst("[itemprop=author]")?.text()
        ?: source
    val pubRaw = doc.selectFirst("meta[property=article:published_time]")?.attr("content")
        ?: doc.selectFirst("time")?.attr("datetime")
        ?: ""

    val articleRoot = doc.selectFirst("article")
        ?: doc.selectFirst("[data-testid=Body]")

    val rawHtml = articleRoot?.html().orEmpty()
    val sanitizedContent = sanitizeContentHtml(rawHtml)

    val title = normalizeTitle(titleRaw)
    val description = normalizeDescription(stripHtml(sanitizedContent).take(200))
    val imageUrl = normalizeImageUrl(imageRaw)
    val timeInMil = parseDateToMillis(pubRaw)

    return FeedItem(
        title = title,
        description = description,
        content = sanitizedContent,
        author = authorRaw.ifBlank { source },
        publishedAt = pubRaw.trim(),
        imageUrl = imageUrl,
        link = url,
        savedDate = "",
        timeInMil = timeInMil,
        isSavedForLater = false
    )
}

private fun extractAP(
    url: String,
    doc: Document,
    source: String
): FeedItem {
    val titleRaw = doc.selectFirst("h1")?.text()
        ?: doc.selectFirst("meta[property=og:title]")?.attr("content")
        ?: ""
    val imageRaw = doc.selectFirst("meta[property=og:image]")?.attr("content")
        ?: ""
    val authorRaw = doc.selectFirst("[data-component=Byline]")?.text()
        ?: source
    val pubRaw = doc.selectFirst("meta[property=article:published_time]")?.attr("content")
        ?: doc.selectFirst("time")?.attr("datetime")
        ?: ""

    val articleRoot = doc.selectFirst("article")
        ?: doc.selectFirst("[data-component=ArticleBody]")

    val rawHtml = articleRoot?.html().orEmpty()
    val sanitizedContent = sanitizeContentHtml(rawHtml)

    val title = normalizeTitle(titleRaw)
    val description = normalizeDescription(stripHtml(sanitizedContent).take(200))
    val imageUrl = normalizeImageUrl(imageRaw)
    val timeInMil = parseDateToMillis(pubRaw)

    return FeedItem(
        title = title,
        description = description,
        content = sanitizedContent,
        author = authorRaw.ifBlank { source },
        publishedAt = pubRaw.trim(),
        imageUrl = imageUrl,
        link = url,
        savedDate = "",
        timeInMil = timeInMil,
        isSavedForLater = false
    )
}

// ---------------------------------------------------------
// READABILITY-STYLE CONTENT SCORING
// ---------------------------------------------------------

private fun findBestContentElement(doc: Document): Element? {
    var bestElement: Element? = null
    var bestScore = Int.MIN_VALUE

    val candidates = doc.select("article, main, section, div")

    for (el in candidates) {
        val score = scoreContentElement(el)
        if (score > bestScore) {
            bestScore = score
            bestElement = el
        }
    }

    return bestElement
}

private fun scoreContentElement(el: Element): Int {
    var score = 0

    val text = el.text()
    val length = text.length

    score += length / 50                       // base by length
    score += el.select("p").size * 5          // paragraph bonus

    val punctuation = text.count { it == '.' || it == ',' || it == ';' }
    score += punctuation                       // punctuation bonus

    val linkCount = el.select("a").size
    val linkDensity = linkCount.toDouble() / (length + 1)
    if (linkDensity > 0.2) score -= 20         // penalize link-heavy

    val classId = (el.className() + " " + el.id()).lowercase()
    if ("article" in classId || "content" in classId || "story" in classId || "post" in classId) {
        score += 30
    }
    if ("comment" in classId || "footer" in classId || "sidebar" in classId || "promo" in classId) {
        score -= 30
    }

    return score
}

// ---------------------------------------------------------
// SANITIZATION + NORMALIZATION
// ---------------------------------------------------------

*/
/**
 * Whitelist-based sanitizer for content HTML.
 * This is what you assign to FeedItem.content.
 *//*

private fun sanitizeContentHtml(raw: String): String {
    if (raw.isBlank()) return ""

    val allowedTags = setOf(
        "p", "strong", "em", "b", "i",
        "ul", "ol", "li", "blockquote", "br",
        "h1", "h2", "h3", "img", "a"
    )

    val doc = Jsoup.parse(raw)

    // Remove disallowed tags by unwrapping
    doc.select("*").forEach { el ->
        if (el.tagName() !in allowedTags) {
            el.unwrap()
        }
    }

    // Remove dangerous attributes; keep only href/src
    doc.select("*").forEach { el ->
        val allowedAttrs = setOf("href", "src")
        el.attributes().asList().forEach { attr ->
            if (attr.key !in allowedAttrs) el.removeAttr(attr.key)
        }
    }

    val cleaned = doc.body().html().trim()
    return cleaned
}

private fun normalizeTitle(raw: String): String {
    if (raw.isBlank()) return ""
    val decoded = Parser.unescapeEntities(raw, false)
    return cleanWhitespace(decoded)
}

private fun normalizeDescription(raw: String): String {
    if (raw.isBlank()) return ""
    val decoded = Parser.unescapeEntities(raw, false)
    val stripped = stripHtml(decoded)
    return cleanWhitespace(stripped)
}

private fun normalizeImageUrl(raw: String?): String {
    if (raw.isNullOrBlank()) return ""

    var url = raw.trim()

    url = url.substringBefore("?")            // drop tracking params

    if (url.startsWith("//")) {
        url = "https:$url"                    // fix schema-less
    }

    if (url.startsWith("data:image")) return "" // drop base64

    if (url.endsWith("1x1.png") || url.endsWith("1x1.jpg")) return "" // drop placeholders

    if (url.contains("googleusercontent.com")) {
        url = url.substringBefore("=w")       // drop resize params
    }

    return url.replace("\\s+".toRegex(), "")
}

private fun stripHtml(raw: String): String {
    return Jsoup.parse(raw).text()
}

private fun cleanWhitespace(raw: String): String {
    return raw.replace("\\s+".toRegex(), " ").trim()
}

// ---------------------------------------------------------
// DATE PARSING → timeInMil
// ---------------------------------------------------------

private fun parseDateToMillis(dateStr: String): Long {
    val trimmed = dateStr.trim()
    if (trimmed.isBlank()) return 0L

    val patterns = listOf(
        "EEE, dd MMM yyyy HH:mm:ss Z",          // RSS pubDate
        "EEE, dd MMM yyyy HH:mm:ss zzz",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssZ",
        "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd"
    )

    for (pattern in patterns) {
        try {
            val sdf = SimpleDateFormat(pattern, Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = sdf.parse(trimmed)
            if (date != null) return date.time
        } catch (_: Exception) {
            // try next
        }
    }

    return 0L
}

// ---------------------------------------------------------
// DEDUPE + SORT
// ---------------------------------------------------------

private fun dedupeByLink(items: List<FeedItem>): List<FeedItem> {
    val seen = mutableSetOf<String>()
    val result = mutableListOf<FeedItem>()
    for (item in items) {
        val key = item.link.ifBlank { item.title }
        if (key.isNotBlank() && seen.add(key)) {
            result += item
        }
    }
    return result
}

private fun sortByPublishedDate(items: List<FeedItem>): List<FeedItem> {
    return items.sortedByDescending { it.timeInMil }
}

// ---------------------------------------------------------
// RETRY HELPER
// ---------------------------------------------------------

private suspend fun <T> retry(
    times: Int,
    block: suspend (attempt: Int) -> T
): T {
    var lastError: Throwable? = null

    repeat(times) { attempt ->
        try {
            return block(attempt + 1)
        } catch (e: Throwable) {
            lastError = e
            val delayMs = 300L * (attempt + 1)
            delay(delayMs)
        }
    }

    throw lastError ?: IllegalStateException("Unknown error in retry()")
}

// ---------------------------------------------------------
// SOURCE HELPERS
// ---------------------------------------------------------

private fun extractDomain(url: String): String {
    return try {
        val host = URI(url).host ?: return ""
        host.removePrefix("www.")
    } catch (_: Exception) {
        ""
    }
}

private fun inferSourceNameFromDomain(domain: String): String {
    return when (domain) {
        "nytimes.com" -> "The New York Times"
        "bbc.com" -> "BBC News"
        "cnn.com" -> "CNN"
        "reuters.com" -> "Reuters"
        "apnews.com" -> "AP News"
        else -> domain.ifBlank { "Unknown Source" }
    }
}

private fun inferSourceNameFromFeed(url: String, doc: Document): String {
    val fromChannel = doc.selectFirst("channel > title")?.text()
        ?: doc.selectFirst("feed > title")?.text()
        ?: ""
    if (fromChannel.isNotBlank()) return fromChannel.trim()

    val domain = extractDomain(url)
    return inferSourceNameFromDomain(domain)
}

*/



/*


// Domains allowed to fall back to HTTP if HTTPS fails
private val httpFallbackWhitelist = setOf(
    "example.com",   // add real HTTP-only domains here
    "legacyfeed.org"
)

// Try HTTPS first
private fun upgradeToHttps(url: String): String {
    return if (url.startsWith("http://", ignoreCase = true)) {
        url.replaceFirst("http://", "https://")
    } else url
}

// Extract domain
private fun domainOf(url: String): String {
    return try {
        java.net.URI(url).host?.removePrefix("www.") ?: ""
    } catch (_: Exception) {
        ""
    }
}
private fun fetchDocumentWithHttpsFallback(url: String, timeoutMs: Int): Document {
    val httpsUrl = upgradeToHttps(url)
    val domain = domainOf(url)

    // 1. Try HTTPS first
    try {
        return Jsoup.connect(httpsUrl)
            .userAgent("Mozilla/5.0 UniversalNewsParser")
            .timeout(timeoutMs)
            .ignoreContentType(true)
            .followRedirects(true)
            .get()
    } catch (e: Exception) {
        logHttpFallback(url, httpsUrl, "HTTPS failed: ${e.message}")
    }

    // 2. If HTTPS failed, only allow HTTP fallback if whitelisted
    if (domain in httpFallbackWhitelist) {
        val httpUrl = httpsUrl.replaceFirst("https://", "http://")
        try {
            logHttpFallback(url, httpsUrl, "Falling back to HTTP (whitelisted)")
            return Jsoup.connect(httpUrl)
                .userAgent("Mozilla/5.0 UniversalNewsParser")
                .timeout(timeoutMs)
                .ignoreContentType(true)
                .followRedirects(true)
                .get()
        } catch (e: Exception) {
            logHttpFallback(url, httpsUrl, "HTTP fallback failed: ${e.message}")
            throw e
        }
    } else {
        logHttpFallback(url, httpsUrl, "Domain not whitelisted — blocking HTTP")
        throw IllegalStateException("HTTP not allowed for domain: $domain")
    }
}


// working parser
suspend fun parseUniversalFeeds(urls: List<String>): List<FeedItem> = withContext(Dispatchers.IO) {

    // -----------------------------
    // CONFIG
    // -----------------------------
    val maxConcurrency = 8
    val maxRetries = 3
    val perFeedTimeoutMs = 15_000L

    // -----------------------------
    // CONCURRENCY LIMITER
    // -----------------------------
    val semaphore = kotlinx.coroutines.sync.Semaphore(maxConcurrency)

    // -----------------------------
    // PARALLEL FETCH + PARSE
    // -----------------------------
    val deferreds = urls.map { url ->
        async {

            semaphore.acquire()

            try {
                retry(maxRetries) { attempt ->

                    withTimeout(perFeedTimeoutMs) {

                        val doc = fetchDocumentWithHttpsFallback(url, perFeedTimeoutMs.toInt())


                        parseSingleDocument(url, doc)
                    }
                }
            } catch (e: Exception) {
                emptyList<FeedItem>()
            } finally {
                semaphore.release()
            }
        }
    }

    val allItems = deferreds.awaitAll().flatten()
    val deduped = dedupeByLink(allItems)
    sortByPublishedDate(deduped)
}


// ---------------------------------------------------------
// CORE DISPATCH
// ---------------------------------------------------------

private fun parseSingleDocument(url: String, doc: Document): List<FeedItem> {
    val rootTag = doc.child(0)?.tagName()?.lowercase(Locale.ROOT) ?: ""

    // RSS
    if (doc.selectFirst("rss, channel, item") != null) {
        return extractRss(doc)
    }

    // Atom
    if (doc.selectFirst("feed > entry") != null || rootTag == "feed") {
        return extractAtom(doc)
    }

    // Google News RSS (uses g:originalLink)
    if (doc.selectFirst("item g\\:originalLink") != null) {
        return extractGoogleNews(doc)
    }

    // Fallback: HTML full article extraction
    val article = extractHtml(url, doc)
    return if (article.title.isNotBlank()) listOf(article) else emptyList()
}

// ---------------------------------------------------------
// RSS
// ---------------------------------------------------------

private fun extractRss(doc: Document): List<FeedItem> {
    val items = mutableListOf<FeedItem>()

    doc.select("item").forEach { itemEl ->
        val link = itemEl.selectFirst("link")?.text()?.trim().orEmpty()
        if (link.isBlank()) return@forEach

        val title = normalizeTitle(
            itemEl.selectFirst("title")?.text().orEmpty()
        )

        val descriptionHtml =
            itemEl.selectFirst("description")?.text().orEmpty()
                .ifBlank { itemEl.selectFirst("content\\:encoded")?.text().orEmpty() }

        val description = normalizeDescription(stripHtml(descriptionHtml))

        val author =
            itemEl.selectFirst("author")?.text().orEmpty()
                .ifBlank { itemEl.selectFirst("dc\\:creator")?.text().orEmpty() }

        val pubRaw =
            itemEl.selectFirst("pubDate")?.text().orEmpty()
                .ifBlank { itemEl.selectFirst("dc\\:date")?.text().orEmpty() }

        val publishedAt = pubRaw.trim()
        val timeInMil = parseDateToMillis(publishedAt)

        val enclosureImage =
            itemEl.selectFirst("enclosure[url]")?.attr("url")
                ?: itemEl.selectFirst("media\\:content[url]")?.attr("url")

        val contentHtml =
            itemEl.selectFirst("content\\:encoded")?.text().orEmpty()
                .ifBlank { descriptionHtml }

        // Try to find image inside HTML if no enclosure
        val docContent = runCatching { Jsoup.parse(contentHtml) }.getOrNull()
        val bodyImage = docContent?.selectFirst("img[src]")?.absUrl("src").orEmpty()

        val imageUrl = normalizeImageUrl(
            enclosureImage
                ?: bodyImage
        )

        items += FeedItem(
            title = title,
            description = description,
            content = sanitizeContentHtml(contentHtml),
            author = author,
            publishedAt = publishedAt,
            imageUrl = imageUrl,
            link = link,
            savedDate = "",
            timeInMil = timeInMil,
            isSavedForLater = false
        )
    }

    return items
}

// ---------------------------------------------------------
// ATOM
// ---------------------------------------------------------

private fun extractAtom(doc: Document): List<FeedItem> {
    val items = mutableListOf<FeedItem>()

    doc.select("entry").forEach { entryEl ->
        val linkEl = entryEl.selectFirst("link[rel=alternate], link[href]")
        val link = linkEl?.attr("href")?.trim().orEmpty()
        if (link.isBlank()) return@forEach

        val title = normalizeTitle(
            entryEl.selectFirst("title")?.text().orEmpty()
        )

        val summaryHtml = entryEl.selectFirst("summary")?.text().orEmpty()
        val contentHtml =
            entryEl.selectFirst("content")?.text().orEmpty()
                .ifBlank { summaryHtml }

        val description = normalizeDescription(
            stripHtml(summaryHtml.ifBlank { contentHtml })
        )

        val author =
            entryEl.selectFirst("author > name")?.text().orEmpty()
                .ifBlank { entryEl.selectFirst("author")?.text().orEmpty() }

        val pubRaw =
            entryEl.selectFirst("updated")?.text().orEmpty()
                .ifBlank { entryEl.selectFirst("published")?.text().orEmpty() }

        val publishedAt = pubRaw.trim()
        val timeInMil = parseDateToMillis(publishedAt)

        val imageUrl = normalizeImageUrl(
            entryEl.selectFirst("media\\:content[url]")?.attr("url")
                ?: entryEl.selectFirst("link[rel=enclosure][type^=image]")?.attr("href")
        )

        items += FeedItem(
            title = title,
            description = description,
            content = sanitizeContentHtml(contentHtml),
            author = author,
            publishedAt = publishedAt,
            imageUrl = imageUrl,
            link = link,
            savedDate = "",
            timeInMil = timeInMil,
            isSavedForLater = false
        )
    }

    return items
}

// ---------------------------------------------------------
// GOOGLE NEWS RSS
// ---------------------------------------------------------

private fun extractGoogleNews(doc: Document): List<FeedItem> {
    val items = mutableListOf<FeedItem>()

    doc.select("item").forEach { itemEl ->
        val link = itemEl.selectFirst("g\\:originalLink")?.text()?.trim().orEmpty()
        if (link.isBlank()) return@forEach

        val title = normalizeTitle(
            itemEl.selectFirst("title")?.text().orEmpty()
        )

        val descriptionHtml =
            itemEl.selectFirst("description")?.text().orEmpty()
        val description = normalizeDescription(stripHtml(descriptionHtml))

        val source = itemEl.selectFirst("source")?.text().orEmpty()

        val pubRaw = itemEl.selectFirst("pubDate")?.text().orEmpty()
        val publishedAt = pubRaw.trim()
        val timeInMil = parseDateToMillis(publishedAt)

        val imageUrl = normalizeImageUrl(
            runCatching {
                val snippetDoc = Jsoup.parse(descriptionHtml)
                snippetDoc.selectFirst("img[src]")?.absUrl("src")
            }.getOrNull()
        )

        items += FeedItem(
            title = title,
            description = description,
            content = sanitizeContentHtml(descriptionHtml),
            author = source,
            publishedAt = publishedAt,
            imageUrl = imageUrl,
            link = link,
            savedDate = "",
            timeInMil = timeInMil,
            isSavedForLater = false
        )
    }

    return items
}

// ---------------------------------------------------------
// FULL HTML ARTICLE EXTRACTION (OPTION C)
// ---------------------------------------------------------


private fun extractHtml(url: String, doc: Document): FeedItem {
    // ---------- OpenGraph ----------
    val ogTitle = doc.selectFirst("meta[property=og:title]")?.attr("content")
    val ogDesc = doc.selectFirst("meta[property=og:description]")?.attr("content")
    val ogImage = doc.selectFirst("meta[property=og:image]")?.attr("content")
    val ogPub = doc.selectFirst("meta[property=article:published_time]")?.attr("content")
    val ogAuthor = doc.selectFirst("meta[property=article:author]")?.attr("content")

    // ---------- Twitter Card ----------
    val twTitle = doc.selectFirst("meta[name=twitter:title]")?.attr("content")
    val twDesc = doc.selectFirst("meta[name=twitter:description]")?.attr("content")
    val twImage =
        doc.selectFirst("meta[name=twitter:image], meta[name=twitter:image:src]")?.attr("content")

    // ---------- JSON-LD ----------
    var ldTitle: String? = null
    var ldDesc: String? = null
    var ldImage: String? = null
    var ldPub: String? = null
    var ldAuthor: String? = null
    var ldBody: String? = null

    doc.select("script[type=application/ld+json]").forEach { script ->
        try {
            val text = script.html()
            if (text.isBlank()) return@forEach

            val jsonAny = JSONObject(text)
            val candidates = mutableListOf<JSONObject>()

            if (jsonAny.has("@type")) {
                candidates += jsonAny
            } else if (jsonAny.has("@graph")) {
                val graph = jsonAny.getJSONArray("@graph")
                for (i in 0 until graph.length()) {
                    val item = graph.optJSONObject(i) ?: continue
                    candidates += item
                }
            }

            candidates.forEach { json ->
                val type = json.optString("@type").lowercase(Locale.ROOT)
                if ("article" in type || "newsarticle" in type || "blogposting" in type) {
                    ldTitle = ldTitle ?: json.optString("headline", null)
                    ldDesc = ldDesc ?: json.optString("description", null)
                    ldPub = ldPub ?: json.optString("datePublished", null)

                    if (ldAuthor == null) {
                        ldAuthor =
                            json.optJSONObject("author")?.optString("name", null)
                                ?: json.optJSONArray("author")?.optJSONObject(0)
                                    ?.optString("name", null)
                    }

                    if (ldImage == null && json.has("image")) {
                        when (val img = json.get("image")) {
                            is String -> ldImage = img
                            is JSONArray -> ldImage = img.optString(0, null)
                            is JSONObject -> ldImage = img.optString("url", null)
                        }
                    }

                    ldBody = ldBody ?: json.optString("articleBody", null)
                }
            }
        } catch (_: Exception) {
        }
    }

    // ---------- Headline ----------
    val h1Title = doc.selectFirst("h1")?.text()

    // ---------- Article Element ----------
    val articleEl =
        doc.selectFirst("article")
            ?: doc.selectFirst("main article")
            ?: doc.selectFirst("div[itemprop=articleBody]")
            ?: doc.selectFirst("div[class*=article-body]")
            ?: doc.selectFirst("div[class*=story-body]")
            ?: doc.selectFirst("div[id*=article-body]")
            ?: doc.selectFirst("section[class*=article]")

    // ---------- Article Body ----------
    val articleBody = when {
        !ldBody.isNullOrBlank() -> ldBody!!
        articleEl != null -> articleEl.select("p").joinToString("\n") { it.text() }
        else -> doc.select("article p, main p, p").joinToString("\n") { it.text() }
    }

    // ---------- Hero Image ----------
    val heroImage =
        ogImage
            ?: twImage
            ?: ldImage
            ?: articleEl?.selectFirst("img[src]")?.absUrl("src")
            ?: doc.selectFirst("img[class*=hero][src]")?.absUrl("src")
            ?: doc.selectFirst("img[class*=featured][src]")?.absUrl("src")
            ?: doc.selectFirst("meta[property=og:image:secure_url]")?.attr("content")
            ?: doc.selectFirst("img[src]")?.absUrl("src")

    // ---------- Description ----------
    val description =
        ogDesc
            ?: twDesc
            ?: ldDesc
            ?: articleEl?.selectFirst("p")?.text()
            ?: doc.selectFirst("article p, main p, p")?.text()
            ?: ""

    // ---------- Published ----------
    val publishedAt =
        ogPub
            ?: ldPub
            ?: doc.selectFirst("time[datetime]")?.attr("datetime")
            ?: doc.selectFirst("meta[name=date], meta[property=article:modified_time]")
                ?.attr("content")
            ?: ""

    // ---------- Author ----------
    val author =
        ogAuthor
            ?: ldAuthor
            ?: doc.selectFirst("meta[name=author]")?.attr("content")
            ?: doc.selectFirst("[class*=author], [itemprop=author]")?.text()
            ?: ""

    // ---------- Final Title ----------
    val title =
        normalizeTitle(
            ogTitle
                ?: twTitle
                ?: ldTitle
                ?: h1Title
                ?: doc.title()
        )

    val cleanedDescription = normalizeDescription(description)
    val contentHtml = sanitizeContentHtml(articleBody)
    val imageUrl = normalizeImageUrl(heroImage)
    val timeInMil = parseDateToMillis(publishedAt)

    return FeedItem(
        title = title,
        description = cleanedDescription,
        content = contentHtml,
        author = author,
        publishedAt = publishedAt,
        imageUrl = imageUrl,
        link = url,
        savedDate = "",
        timeInMil = timeInMil,
        isSavedForLater = false
    )
}

// ---------------------------------------------------------
// NORMALIZATION & SANITIZATION
// ---------------------------------------------------------

private fun normalizeTitle(raw: String): String {
    if (raw.isBlank()) return ""
    val decoded = org.jsoup.parser.Parser.unescapeEntities(raw, false)
    return cleanWhitespace(decoded)
}

private fun normalizeDescription(raw: String): String {
    if (raw.isBlank()) return ""
    val decoded = org.jsoup.parser.Parser.unescapeEntities(raw, false)
    val stripped = stripHtml(decoded)
    return cleanWhitespace(stripped)
}


private fun normalizeImageUrl(raw: String?): String {
    if (raw.isNullOrBlank()) return ""

    var url = raw.trim()

    // Remove tracking parameters
    url = url.substringBefore("?")

    // Fix schema-less URLs: //cdn.site.com/img.jpg
    if (url.startsWith("//")) {
        url = "https:$url"
    }

    // Reject base64 images
    if (url.startsWith("data:image")) return ""

    // Reject tiny placeholder images
    if (url.endsWith("1x1.png") || url.endsWith("1x1.jpg")) return ""

    // Google AMP / proxy cleanup
    if (url.contains("googleusercontent.com")) {
        url = url.substringBefore("=w") // remove resizing params
    }

    // Normalize whitespace
    return url.replace("\\s+".toRegex(), "")
}


private fun sanitizeContentHtml(raw: String): String {
    // For now: just trim. You can extend to whitelist tags.
    return raw.trim()
}

private fun stripHtml(raw: String): String {
    if (raw.isBlank()) return ""
    return Jsoup.parse(raw).text()
}

private fun cleanWhitespace(raw: String): String {
    return raw.replace("\\s+".toRegex(), " ").trim()
}

// ---------------------------------------------------------
// UTILS: DATE, DEDUPE, SORT
// ---------------------------------------------------------

private fun parseDateToMillis(dateStr: String): Long {
    val trimmed = dateStr.trim()
    if (trimmed.isBlank()) return System.currentTimeMillis()

    val patterns = listOf(
        "EEE, dd MMM yyyy HH:mm:ss Z",          // RSS pubDate
        "EEE, dd MMM yyyy HH:mm:ss zzz",        // Alternative RSS
        "yyyy-MM-dd'T'HH:mm:ss'Z'",             // ISO Zulu
        "yyyy-MM-dd'T'HH:mm:ssXXX",             // ISO with timezone offset
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",         // ISO with millis (Zulu)
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",         // ISO with millis + offset
        "yyyy-MM-dd'T'HH:mm:ssZ",               // ISO without colon in offset
        "yyyy-MM-dd'T'HH:mm:ss.SSSZ",           // ISO millis without colon
        "yyyy-MM-dd'T'HH:mm:ss",                // ISO no timezone
        "yyyy-MM-dd HH:mm:ss",                  // Common HTML meta format
        "yyyy-MM-dd"                            // Date only
    )

    for (pattern in patterns) {
        try {
            val sdf = SimpleDateFormat(pattern, Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = sdf.parse(trimmed)
            if (date != null) return date.time
        } catch (_: Exception) {
            // try next pattern
        }
    }

    // Parsing failed → return 0 so UI can show empty string
    return System.currentTimeMillis()
}


private fun dedupeByLink(items: List<FeedItem>): List<FeedItem> {
    val seen = LinkedHashMap<String, FeedItem>()
    for (item in items) {
        val key = item.link.trim()
        if (key.isBlank()) continue
        if (!seen.containsKey(key)) {
            seen[key] = item
        }
    }
    return seen.values.toList()
}

private fun sortByPublishedDate(items: List<FeedItem>): List<FeedItem> {
    return items.sortedByDescending { it.timeInMil }
}


private suspend fun <T> retry(
    times: Int,
    block: suspend (attempt: Int) -> T
): T {
    var lastError: Throwable? = null

    repeat(times) { attempt ->
        try {
            return block(attempt + 1)
        } catch (e: Throwable) {
            lastError = e
            val delayMs = 300L * (attempt + 1) // exponential-ish backoff
            kotlinx.coroutines.delay(delayMs)
        }
    }

    throw lastError ?: IllegalStateException("Unknown error in retry()")
}



// =========================================================
// AGGREGATOR DETECTION
// =========================================================

private val AGGREGATOR_DOMAINS = setOf(
    "news.google.com",
    "news.yahoo.com",
    "rss.news.yahoo.com",
    "msn.com",
    "flipboard.com",
    "apple.news"
)

private fun FeedItem.sourceDomain(): String =
    extractDomain(this.link)

private fun FeedItem.isAggregator(): Boolean =
    sourceDomain() in AGGREGATOR_DOMAINS

private fun FeedItem.isPrimarySource(): Boolean {
    val linkDomain = extractDomain(this.link)
    return !isAggregator() || linkDomain !in AGGREGATOR_DOMAINS
}

// =========================================================
// CONFIDENCE SCORING (COMPUTED, NOT STORED)
// =========================================================

fun computeConfidenceScore(item: FeedItem): Int {
    var score = 0

    // Content quality
    if (item.title.length > 15) score += 10
    if (item.description.length > 50) score += 10
    if (item.content.length > 300) score += 20
    if (item.content.length > 800) score += 10

    // Metadata completeness
    if (item.author.isNotBlank()) score += 10
    if (item.imageUrl.isNotBlank()) score += 10
    if (item.timeInMil > 0) score += 10

    // Source quality
    if (item.isPrimarySource()) score += 20

    return score.coerceIn(0, 100)
}

// =========================================================
// CANONICAL URL NORMALIZATION
// =========================================================

fun canonicalizeLink(url: String): String {
    return try {
        val uri = URI(url)
        val cleanQuery = uri.query
            ?.split("&")
            ?.filterNot {
                it.startsWith("utm_") ||
                        it.startsWith("fbclid") ||
                        it.startsWith("gclid")
            }
            ?.joinToString("&")

        URI(uri.scheme, uri.authority, uri.path, cleanQuery, null).toString()
    } catch (_: Exception) {
        url.trim()
    }
}

// =========================================================
// CONTENT FINGERPRINT (CROSS-SOURCE)
// =========================================================

private fun contentFingerprint(item: FeedItem): String {
    val base = buildString {
        append(item.title.lowercase())
        append("|")
        append(item.description.take(200).lowercase())
    }

    return MessageDigest.getInstance("SHA-256")
        .digest(base.toByteArray())
        .joinToString("") { "%02x".format(it) }
}

// =========================================================
// NOTIFICATION-SAFE DEDUPE (BEST VERSION WINS)
// =========================================================

fun dedupeForNotifications(items: List<FeedItem>): List<FeedItem> {
    val bestByFingerprint = LinkedHashMap<String, FeedItem>()

    for (item in items) {
        val key = contentFingerprint(item)
        val existing = bestByFingerprint[key]

        if (
            existing == null ||
            computeConfidenceScore(item) > computeConfidenceScore(existing)
        ) {
            bestByFingerprint[key] = item
        }
    }

    return bestByFingerprint.values.toList()
}


private fun extractDomain(url: String): String =
    try {
        URI(url).host?.removePrefix("www.") ?: ""
    } catch (_: Exception) {
        ""
    }


private fun logHttpFallback(originalUrl: String, httpsUrl: String, reason: String) {
    val domain = domainOf(originalUrl)
    println("HTTP-FALLBACK: domain=$domain | https=$httpsUrl | reason=$reason")
}
*/


/*
suspend fun parseUniversalFeeds(
    urls: List<String>,
    maxDescriptionLength: Int = 300
): List<FeedItem> = coroutineScope {

    // ---------------------------------------------------------
    // HELPERS
    // ---------------------------------------------------------

    fun normalizeLink(link: String?): String =
        link?.substringBefore("?")?.trim().orEmpty()

    fun normalizeImageUrl(url: String?): String? {
        if (url.isNullOrBlank()) return null
        val clean = url.substringBefore("?").trim()
        return when {
            clean.startsWith("//") -> "https:$clean"
            clean.startsWith("http") -> clean
            else -> null
        }
    }

    fun normalizeTitle(title: String?): String =
        Jsoup.parse(title ?: "")
            .text()
            .replace("\\s+".toRegex(), " ")
            .trim()

    fun stripHtml(html: String?): String? =
        html?.let { Jsoup.parse(it).text().trim() }

    fun normalizeDescription(desc: String?, maxLen: Int): String {
        if (desc.isNullOrBlank()) return ""
        val clean = stripHtml(desc) ?: ""
        return if (clean.length > maxLen) clean.take(maxLen) + "…" else clean
    }

    fun sanitizeContentHtml(html: String?): String {
        if (html.isNullOrBlank()) return ""
        val doc = Jsoup.parse(html)

        doc.select("script, style, iframe, noscript, header, footer, ads, .ad, .advert").remove()

        doc.select("*").forEach { el ->
            el.clearAttributes()
        }

        return doc.body().html().trim()
    }

    // ---------------------------------------------------------
    // GOOGLE NEWS IMAGE EXTRACTOR
    // ---------------------------------------------------------
    fun extractGoogleImage(element: Element): String {

        fun clean(url: String?): String? =
            url
                ?.substringBefore("?")
                ?.takeIf { it.startsWith("http") }

        clean(
            element.getElementsByTag("media:content")
                .firstOrNull()
                ?.attr("url")
        )?.let { return it }

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

    // ---------------------------------------------------------
    // DATE PARSER
    // ---------------------------------------------------------
    val dateFormats = listOf(
        "EEE, dd MMM yyyy HH:mm:ss Z",
        "EEE, dd MMM yyyy HH:mm Z",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ssZ",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
    ).map { pattern ->
        SimpleDateFormat(pattern, Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    fun parseDate(raw: String?): Long? {
        if (raw.isNullOrBlank()) return null
        for (fmt in dateFormats) {
            try {
                return fmt.parse(raw)?.time
            } catch (_: Exception) {
            }
        }
        return null
    }

    // ---------------------------------------------------------
    // PARSE A SINGLE FEED
    // ---------------------------------------------------------
    suspend fun parseSingle(url: String): List<FeedItem> =
        withContext(Dispatchers.IO) {

            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 UniversalRssParser")
                .timeout(15000)
                .get()

            val isGoogleFeed = url.contains("news.google.com")

            when {
                // ---------------------------------------------------------
                // RSS 2.0
                // ---------------------------------------------------------
                doc.select("rss > channel > item").isNotEmpty() -> {
                    doc.select("rss > channel > item").map { el ->

                        val title = normalizeTitle(el.selectFirst("title")?.text())

                        val link = normalizeLink(el.selectFirst("link")?.text())

                        val rawDesc = el.selectFirst("description")?.text()
                        val description = normalizeDescription(rawDesc, maxDescriptionLength)

                        val rawContent = el.selectFirst("content|encoded")?.text() ?: rawDesc
                        val content = sanitizeContentHtml(rawContent)

                        val author = el.selectFirst("author")?.text().orEmpty()

                        val publishedAtStr =
                            el.selectFirst("pubDate")?.text()
                                ?: el.selectFirst("dc|date")?.text()
                                ?: ""

                        val timeInMil = parseDate(publishedAtStr) ?: 0L

                        val rawImage =
                            if (isGoogleFeed) extractGoogleImage(el)
                            else (
                                    el.selectFirst("media|thumbnail")?.attr("url")
                                        ?: el.selectFirst("enclosure[url][type^=image]")
                                            ?.attr("url")
                                        ?: extractGoogleImage(el)
                                    )

                        val imageUrl = normalizeImageUrl(rawImage).orEmpty()

                        FeedItem(
                            title = title,
                            description = description,
                            content = content,
                            author = author,
                            publishedAt = publishedAtStr,
                            imageUrl = imageUrl,
                            link = link,
                            savedDate = "",
                            timeInMil = timeInMil,
                            isSavedForLater = false
                        )
                    }
                }

                // ---------------------------------------------------------
                // ATOM
                // ---------------------------------------------------------
                doc.select("feed > entry").isNotEmpty() -> {
                    doc.select("feed > entry").map { el ->

                        val title = normalizeTitle(el.selectFirst("title")?.text())

                        val rawLink = el.select("link").firstOrNull { l ->
                            val rel = l.attr("rel")
                            rel.isEmpty() || rel == "alternate"
                        }?.attr("href")
                        val link = normalizeLink(rawLink)

                        val rawSummary = el.selectFirst("summary")?.text()
                        val description = normalizeDescription(rawSummary, maxDescriptionLength)

                        val rawContent = el.selectFirst("content")?.text() ?: rawSummary
                        val content = sanitizeContentHtml(rawContent)

                        val author = el.selectFirst("author > name")?.text().orEmpty()

                        val publishedAtStr =
                            el.selectFirst("updated")?.text()
                                ?: el.selectFirst("published")?.text()
                                ?: ""

                        val timeInMil = parseDate(publishedAtStr) ?: 0L

                        val rawImage =
                            if (isGoogleFeed) extractGoogleImage(el)
                            else extractGoogleImage(el)

                        val imageUrl = normalizeImageUrl(rawImage).orEmpty()

                        FeedItem(
                            title = title,
                            description = description,
                            content = content,
                            author = author,
                            publishedAt = publishedAtStr,
                            imageUrl = imageUrl,
                            link = link,
                            savedDate = "",
                            timeInMil = timeInMil,
                            isSavedForLater = false
                        )
                    }
                }

                // ---------------------------------------------------------
                // HTML FALLBACK
                // ---------------------------------------------------------
                else -> {
                    val articles = doc.select("article, div[class*=post], div[class*=story]")
                    articles.mapNotNull { el ->
                        val titleEl = el.selectFirst("h1, h2, h3, a[rel=bookmark], a[href]")
                            ?: return@mapNotNull null

                        val title = normalizeTitle(titleEl.text())

                        val link = normalizeLink(titleEl.absUrl("href").ifEmpty { url })

                        val rawDesc = el.selectFirst("p")?.text()
                        val description = normalizeDescription(rawDesc, maxDescriptionLength)

                        val rawImage = el.selectFirst("img[src]")?.absUrl("src")
                        val imageUrl = normalizeImageUrl(rawImage).orEmpty()

                        val content = sanitizeContentHtml(el.html())

                        FeedItem(
                            title = title,
                            description = description,
                            content = content,
                            author = "",
                            publishedAt = "",
                            imageUrl = imageUrl,
                            link = link,
                            savedDate = "",
                            timeInMil = 0L,
                            isSavedForLater = false
                        )
                    }
                }
            }
        }

    // ---------------------------------------------------------
    // PARSE ALL FEEDS IN PARALLEL
    // ---------------------------------------------------------
    val jobs = urls.map { url ->
        async { runCatching { parseSingle(url) }.getOrElse { emptyList() } }
    }

    jobs.awaitAll()
        .flatten()
        .distinctBy { it.link }
        .sortedByDescending { it.timeInMil }
}
*/
