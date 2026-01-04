package com.ola.fivethirtyeight.feedValidation

import okhttp3.ResponseBody
import org.jsoup.nodes.Document
import retrofit2.Response

sealed class FeedValidationResult {
    object Valid : FeedValidationResult()
    data class Invalid(val reason: String) : FeedValidationResult()
}

fun validateRssResponse(
    response: Response<ResponseBody>
): FeedValidationResult {

    if (!response.isSuccessful) {
        return FeedValidationResult.Invalid(
            "HTTP ${response.code()}"
        )
    }

    val body = response.body()?.string()
        ?: return FeedValidationResult.Invalid("Empty body")

    if (!body.contains("<rss") && !body.contains("<feed")) {
        return FeedValidationResult.Invalid("Not RSS/XML")
    }

    return FeedValidationResult.Valid
}


fun validateParsedFeed(doc: Document): FeedValidationResult {
    val items = doc.select("item")

    if (items.isEmpty()) {
        return FeedValidationResult.Invalid("No <item> elements")
    }

    return FeedValidationResult.Valid
}
