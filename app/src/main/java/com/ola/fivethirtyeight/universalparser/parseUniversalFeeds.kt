@file:Suppress("unused")
package com.ola.fivethirtyeight.universalparser

import com.ola.fivethirtyeight.R
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

        val imageUrl = filterAndNormalizeImageUrl(
            enclosureImage ?: bodyImage
        )



        items += FeedItem(
            title = title,
            description = description,
            content = sanitizeContentHtml(contentHtml),
            author = author,
            publishedAt = publishedAt,
            imageUrl = imageUrl ?: "",
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

        val imageUrl = filterAndNormalizeImageUrl(
            entryEl.selectFirst("media\\:content[url]")?.attr("url")
                ?: entryEl.selectFirst("link[rel=enclosure][type^=image]")?.attr("href")
        )



        items += FeedItem(
            title = title,
            description = description,
            content = sanitizeContentHtml(contentHtml),
            author = author,
            publishedAt = publishedAt,
            imageUrl = imageUrl ?: "",
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

        val imageUrl = filterAndNormalizeImageUrl(
            runCatching {
                Jsoup.parse(descriptionHtml)
                    .selectFirst("img[src]")
                    ?.absUrl("src")
            }.getOrNull()
        )


        items += FeedItem(
            title = title,
            description = description,
            content = sanitizeContentHtml(descriptionHtml),
            author = source,
            publishedAt = publishedAt,
            imageUrl = imageUrl ?: "",
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
    val imageUrl = filterAndNormalizeImageUrl(heroImage)


    val timeInMil = parseDateToMillis(publishedAt)

    return FeedItem(
        title = title,
        description = cleanedDescription,
        content = contentHtml,
        author = author,
        publishedAt = publishedAt,
        imageUrl = (imageUrl ?: R.drawable.fivessss1).toString(),
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

/*private fun normalizeImageUrl(raw: String?): String {
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
}*/
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


private fun filterAndNormalizeImageUrl(raw: String?): String? {
    if (raw.isNullOrBlank()) return null

    val normalized = normalizeImageUrl(raw)
    if (normalized.isBlank()) return null

    val lower = normalized.lowercase()

    // ---- Universal junk ----
    if (
        lower.contains("placeholder") ||
        lower.contains("default") ||
        lower.contains("spacer") ||
        lower.contains("pixel") ||
        lower.contains("tracking") ||
        lower.contains("transparent") ||
        lower.endsWith(".svg")
    ) return null

    // ---- NPR specific ----
    if (
        lower.contains("npr.org") &&
        (
                lower.contains("empty") ||
                        lower.contains("generic") ||
                        lower.contains("station") ||
                        lower.contains("logo")
                )
    ) return null

    // ---- Yahoo specific ----
    if (
        lower.contains("yahoo") &&
        (
                lower.contains("resize") ||
                        lower.contains("crop") ||
                        lower.contains("blank") ||
                        lower.contains("placeholder")
                )
    ) return null

    // ---- Known 1x1 / tiny assets ----
    if (
        lower.endsWith("1x1.png") ||
        lower.endsWith("1x1.jpg")
    ) return null

    return normalized
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

