package com.ola.fivethirtyeight.model


fun FeedItemEntity.toFeedItem(): FeedItem {
    return FeedItem(
        link = link,
        title = title,
        description = description,
        publishedAt = publishedAt,
        imageUrl = imageUrl,
        content = content,
        savedDate = savedDate,
        author = author,
        timeInMil = timeInMil


    )
}


fun PoliticsItemEntity.toFeedItem(): FeedItem {
    return FeedItem(
        link = link,
        title = title,
        description = description,
        publishedAt = publishedAt,
        imageUrl = imageUrl,
        content = content,
        savedDate = savedDate,
        author = author,
        timeInMil = timeInMil)
}

fun WorldItemEntity.toFeedItem(): FeedItem {
    return FeedItem(
        link = link,
        title = title,
        description = description,
        publishedAt = publishedAt,
        imageUrl = imageUrl,
        content = content,
        savedDate = savedDate,
        author = author,
        timeInMil = timeInMil)
}

fun BusinessItemEntity.toFeedItem(): FeedItem {
    return FeedItem(
        link = link,
        title = title,
        description = description,
        publishedAt = publishedAt,
        imageUrl = imageUrl,
        content = content,
        savedDate = savedDate,
        author = author,
        timeInMil = timeInMil)
}


fun SportsItemEntity.toFeedItem(): FeedItem {
    return FeedItem(
        link = link,
        title = title,
        description = description,
        publishedAt = publishedAt,
        imageUrl = imageUrl,
        content = content,
        savedDate = savedDate,
        author = author,
        timeInMil = timeInMil)
}


fun TechItemEntity.toFeedItem(): FeedItem {
    return FeedItem(
        link = link,
        title = title,
        description = description,
        publishedAt = publishedAt,
        imageUrl = imageUrl,
        content = content,
        savedDate = savedDate,
        author = author,
        timeInMil = timeInMil)
}



fun HealthItemEntity.toFeedItem(): FeedItem {
    return FeedItem(
        link = link,
        title = title,
        description = description,
        publishedAt = publishedAt,
        imageUrl = imageUrl,
        content = content,
        savedDate = savedDate,
        author = author,
        timeInMil = timeInMil)
}

fun FiveThirtyEightItemEntity.toFeedItem(): FeedItem {
    return FeedItem(
        link = link,
        title = title,
        description = description,
        publishedAt = publishedAt,
        imageUrl = imageUrl,
        content = content,
        savedDate = savedDate,
        author = author,
        timeInMil = timeInMil)
}
