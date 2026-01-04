package com.ola.fivethirtyeight.model


fun FeedItem.toEntity(): FeedItemEntity {
    return FeedItemEntity(
        link = link,
        title = title,
        description = description,
        publishedAt = publishedAt,
        imageUrl = imageUrl,
        content = content,
        savedDate = savedDate,
        author = author,
        timeInMil = timeInMil,
        isSavedForLater = isSavedForLater,


     )
}


fun FeedItem.toPolEntity(): PoliticsItemEntity {
    return PoliticsItemEntity(
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

fun FeedItem.toWorldEntity(): WorldItemEntity {
    return WorldItemEntity(
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

fun FeedItem.toBusinessEntity(): BusinessItemEntity {
    return BusinessItemEntity(
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

fun FeedItem.toSportsEntity(): SportsItemEntity {
    return SportsItemEntity(
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

fun FeedItem.toTechEntity(): TechItemEntity {
    return TechItemEntity(
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

fun FeedItem.toHealthEntity(): HealthItemEntity {
    return HealthItemEntity(
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
fun FeedItem.toFiveThirtyEightEntity(): FiveThirtyEightItemEntity {
    return FiveThirtyEightItemEntity(
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