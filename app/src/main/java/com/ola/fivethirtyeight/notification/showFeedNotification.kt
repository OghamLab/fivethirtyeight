package com.ola.fivethirtyeight.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.ola.fivethirtyeight.R
import com.ola.fivethirtyeight.model.FeedItem


// -------------------------------
// CHANNELS
// -------------------------------
const val CHANNEL_ID = "news_channel_id"
const val BREAKING_CHANNEL_ID = "breaking_news_channel"
const val NOTIF_ID = 1001
const val SUMMARY_ID = 1000
private const val GROUP_BREAKING = "breaking_group"

private fun createBreakingChannel(manager: NotificationManager, context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            BREAKING_CHANNEL_ID,
            "Breaking News",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Urgent breaking news alerts"
            enableLights(true)
            lightColor = Color.RED
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 300, 150, 300)
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        }
        manager.createNotificationChannel(channel)
    }
}

// -------------------------------
// MAIN NOTIFICATION FUNCTION
// -------------------------------
fun showFeedNotification(
    newItems: List<FeedItem>,
    context: Context
) {
    if (newItems.isEmpty()) return

    val now = System.currentTimeMillis()
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Create both channels
    createNotificationChannelIfNeeded(manager, context)
    createBreakingChannel(manager, context)

    // -------------------------------
    // BREAKING NEWS DETECTION
    // -------------------------------
    val breaking = newItems.filter { isBreaking(it, now) }

    if (breaking.isNotEmpty()) {
        if (breaking.size == 1) {
            showBreakingNotification(breaking.first(), context, manager)
        } else {
            showBreakingSummary(breaking, context, manager)
        }
        return
    }

    // -------------------------------
    // NORMAL TOP STORIES NOTIFICATION
    // -------------------------------
    val article = newItems.first()

    val deepLinkUri = ("fivethirtyeight://article/" +
            Uri.encode(article.title) + "/" +
            Uri.encode(article.link)).toUri()

    val intent = Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.notification_news)
        .setContentTitle("Top Stories")
        .setContentText(article.title)
        .setStyle(
            NotificationCompat.BigTextStyle()
                .bigText(article.description)
                .setBigContentTitle("Top Stories")
        )
        .setContentIntent(pendingIntent)
        .setColor(context.getColor(R.color.teal_200))
        .setColorized(true)
        .setAutoCancel(true)
        .build()

    manager.notify(NOTIF_ID, notification)
}

// -------------------------------
// BREAKING NEWS NOTIFICATIONS
// -------------------------------
private fun showBreakingNotification(
    item: FeedItem,
    context: Context,
    manager: NotificationManager
) {
    val deepLinkUri = ("fivethirtyeight://article/" +
            Uri.encode(item.title) + "/" +
            Uri.encode(item.link)).toUri()

    val intent = Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    val pendingIntent = PendingIntent.getActivity(
        context, 0, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notif = NotificationCompat.Builder(context, BREAKING_CHANNEL_ID)
        .setSmallIcon(R.drawable.fivessss1)
        .setColor(Color.RED)
        .setColorized(true)
        .setContentTitle("Breaking News")
        .setContentText(item.title)
        .setStyle(
            NotificationCompat.BigTextStyle()
                .bigText(item.description)
                .setBigContentTitle("Breaking News")
        )
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .build()

    manager.notify(NOTIF_ID, notif)
}

private fun showBreakingSummary(
    items: List<FeedItem>,
    context: Context,
    manager: NotificationManager
) {
    /*val = NotificationCompat.InboxStyle().apply {
        items.take(5).forEach { addLine(it.title) }
    }*/

    val bigText = items
        .take(5)
        .joinToString(separator = "\n") { "• ${it.title}" }

    val style = NotificationCompat.BigTextStyle()
        .bigText(bigText)


    val summary = NotificationCompat.Builder(context, BREAKING_CHANNEL_ID)
        .setSmallIcon(R.drawable.fivessss1)
        .setColor(Color.RED)
        .setColorized(true)
        .setContentTitle("Breaking News Update")
        .setContentText("${items.size} new breaking stories")
        .setStyle(style)
        .setGroup(GROUP_BREAKING)
        .setGroupSummary(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .build()

    manager.notify(SUMMARY_ID, summary)
}


private fun createNotificationChannelIfNeeded(
    manager: NotificationManager,
    context: Context
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val soundUri: Uri =
            "android.resource://${context.packageName}/raw/notify_sound".toUri()

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()


        val channel = NotificationChannel(
            CHANNEL_ID,
            "Top Stories",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for new top stories"
            enableLights(true)
            lightColor = 0xFF4CAF50.toInt()
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 200, 100, 200)
            setSound(soundUri, audioAttributes)
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        }
        manager.createNotificationChannel(channel)
    }
}



const val ACTION_OPEN_ARTICLE = "com.ola.fivethirtyeight.OPEN_ARTICLE"
const val EXTRA_URL = "extra_article_url"
const val EXTRA_TITLE = "extra_article_title"

private const val GROUP_NEWS = "group_news"



/*

fun showFeedNotification(
    newItems: List<FeedItem>,
    context: Context
) {
    if (newItems.isEmpty()) return

    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    createNotificationChannelIfNeeded(notificationManager, context)

    val article = newItems.first()
    val intent = Intent(context, MainActivity::class.java).apply {
        action = ACTION_OPEN_ARTICLE
        putExtra(EXTRA_URL, article.link)
        putExtra(EXTRA_TITLE, article.title)
        flags  = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

    )

    val bigTextStyle = NotificationCompat.BigTextStyle()
        .bigText(article.description)
        .setBigContentTitle("Top Stories")
        .setSummaryText("Tap to read more")

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.notification_news)
        .setContentTitle("Top Stories")
        .setContentText(article.title)
        .setColor(ContextCompat.getColor(context, R.color.teal_200))
        .setColorized(true)
        .setStyle(bigTextStyle)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    notificationManager.notify(NOTIF_ID, notification)
}
*/



/*
fun showFeedNotification(
    newItems: List<FeedItem>,
    context: Context
) {
    if (newItems.isEmpty()) return

    val article = newItems.first()

    // ✅ 1. Build INTERNAL deep link (not real URL)
    val deepLinkUri = ("fivethirtyeight://article/" +
            Uri.encode(article.title) + "/" +
            Uri.encode(article.link)).toUri()

    Log.d("NOTIF", deepLinkUri.toString())




    // ✅ 2. Intent MUST be ACTION_VIEW
    val intent = Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    // ✅ 3. PendingIntent
    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.notification_news)
        .setContentTitle("Top Stories")
        .setContentText(article.title)
        .setStyle(
            NotificationCompat.BigTextStyle()
                .bigText(article.description)
                .setBigContentTitle("Top Stories")
        )
        .setContentIntent(pendingIntent)
        .setColor(context.getColor(R.color.teal_200))
        .setColorized(true)
        .setAutoCancel(true)
        .build()

    val manager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    createNotificationChannelIfNeeded(manager, context)
    manager.notify(NOTIF_ID, notification)


}
*/


/*un showFeedNotification(
    newItems: List<FeedItem>,
    context: Context
) {
    if (newItems.isEmpty()) return

    val now = System.currentTimeMillis()
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Create both channels
    createNotificationChannelIfNeeded(manager, context)
    createBreakingChannel(manager, context)

    // ---- BREAKING NEWS DETECTION ----
    val breaking = newItems.filter { isBreaking (it, now) }
    val highPriority = breaking.filter { it.priority().alwaysNotify }
    val normalBreaking = breaking - highPriority

    // ---- HIGH PRIORITY BREAKING ----
    if (highPriority.isNotEmpty()) {
        if (highPriority.size == 1)
            showBreakingNotification(highPriority.first(), context, manager)
        else
            showBreakingSummary(highPriority, context, manager)
        return
    }

    // ---- NORMAL BREAKING ----
    if (normalBreaking.isNotEmpty()) {
        if (normalBreaking.size == 1)
            showBreakingNotification(normalBreaking.first(), context, manager)
        else
            showBreakingSummary(normalBreaking, context, manager)
        return
    }

    // ---- FALLBACK: NORMAL TOP STORIES ----
    val article = newItems.first()
    val deepLinkUri = ("fivethirtyeight://article/" +
            Uri.encode(article.title) + "/" +
            Uri.encode(article.link)).toUri()

    val intent = Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    val pendingIntent = PendingIntent.getActivity(
        context, 0, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.notification_news)
        .setContentTitle("Top Stories")
        .setContentText(article.title)
        .setStyle(
            NotificationCompat.BigTextStyle()
                .bigText(article.description)
                .setBigContentTitle("Top Stories")
        )
        .setContentIntent(pendingIntent)
        .setColor(context.getColor(R.color.teal_200))
        .setColorized(true)
        .setAutoCancel(true)
        .build()

    manager.notify(NOTIF_ID, notification)
}

*//*fun isBreaking(item: FeedItem, now: Long): Boolean {
    val published = item.timeInMil ?: return false
    val priority = item.priority()
    return now - published <= priority.breakingWindowMs
}*//*
*//*
fun showFeedNotification(
    newItems: List<FeedItem>,
    context: Context
) {
    if (newItems.isEmpty()) return

    val manager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    createNotificationChannelIfNeeded(manager, context)

    // 🔔 Post ONE notification per article (unique ID)
    newItems.forEach { article ->

        val deepLinkUri = (
                "fivethirtyeight://article/" +
                        Uri.encode(article.title) + "/" +
                        Uri.encode(article.link)
                ).toUri()

        val intent = Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            article.link.hashCode(), // 👈 unique per article
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        



        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_news)
            .setContentTitle("Top Stories")
            .setContentText(article.title)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(article.description)
            )
            .setContentIntent(pendingIntent)
            .setGroup(GROUP_NEWS)

            // 🔕 CHILD MUST NOT ALERT
            .setOnlyAlertOnce(true)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)

            .setAutoCancel(true)
            .setColor(context.getColor(R.color.teal_200))
            .setColorized(true)
            .build()
        manager.notify(
            article.link.hashCode(), // 👈 DIFFERENT ID = stack
            notification
        )



    }

    val summaryNotification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.notification_news)
        .setContentTitle("Top Stories")
        .setContentText("${newItems.size} new stories")

        .setGroup(GROUP_NEWS)
        .setGroupSummary(true)

        // 🔔 SUMMARY alerts once
        .setOnlyAlertOnce(true)
        .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)

        // 📦 Correct category
       // .setCategory(NotificationCompat.)

        .setPriority(NotificationCompat.PRIORITY_HIGH)

        .setStyle(
            NotificationCompat.InboxStyle().also { style ->
                newItems.take(5).forEach { item ->
                    style.addLine(item.title)
                }
                if (newItems.size > 5) {
                    style.addLine("+${newItems.size - 5} more")
                }
            }
        )
        .build()

    manager.notify(SUMMARY_ID, summaryNotification)

}
*//*




    private fun createNotificationChannelIfNeeded(
    manager: NotificationManager,
    context: Context
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val soundUri: Uri =
            "android.resource://${context.packageName}/raw/notify_sound".toUri()

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()


        val channel = NotificationChannel(
            CHANNEL_ID,
            "Top Stories",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for new top stories"
            enableLights(true)
            lightColor = 0xFF4CAF50.toInt()
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 200, 100, 200)
            setSound(soundUri, audioAttributes)
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        }
        manager.createNotificationChannel(channel)
    }
}

private fun showBreakingNotification(
    item: FeedItem,
    context: Context,
    manager: NotificationManager
) {
    val deepLinkUri = ("fivethirtyeight://article/" +
            Uri.encode(item.title) + "/" +
            Uri.encode(item.link)).toUri()

    val intent = Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    val pendingIntent = PendingIntent.getActivity(
        context, 0, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notif = NotificationCompat.Builder(context, BREAKING_CHANNEL_ID)
        .setSmallIcon(R.drawable.fivessss1)   // 🔥 special icon
        .setColor(Color.RED)                         // 🔥 breaking color
        .setColorized(true)
        .setContentTitle("Breaking News")
        .setContentText(item.title)
        .setStyle(
            NotificationCompat.BigTextStyle()
                .bigText(item.description)
                .setBigContentTitle("Breaking News")
        )
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .build()

    manager.notify(NOTIF_ID, notif)
}


const val BREAKING_CHANNEL_ID = "breaking_news_channel"

private fun createBreakingChannel(manager: NotificationManager, context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

        val channel = NotificationChannel(
            BREAKING_CHANNEL_ID,
            "Breaking News",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Urgent breaking news alerts"
            enableLights(true)
            lightColor = Color.RED
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 300, 150, 300)
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        }

        manager.createNotificationChannel(channel)
    }
}


private fun showBreakingSummary(
    items: List<FeedItem>,
    context: Context,
    manager: NotificationManager
) {
    *//*val style = NotificationCompat.InboxStyle().apply {
        items.take(5).forEach{  }
    }*//*

    val summary = NotificationCompat.Builder(context, BREAKING_CHANNEL_ID)
        .setSmallIcon(R.drawable.picsart)
        .setColor(Color.RED)
        .setColorized(true)
        .setContentTitle("Breaking News Update")
        .setContentText("${items.size} new breaking stories")
        //.setStyle(style)
        .setGroup("breaking_group")
        .setGroupSummary(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .build()

    manager.notify(SUMMARY_ID, summary)
}




const val CHANNEL_ID = "news_channel_id"
const val NOTIF_ID = 1001
const val ACTION_OPEN_ARTICLE = "com.ola.fivethirtyeight.OPEN_ARTICLE"
const val EXTRA_URL = "extra_article_url"
const val EXTRA_TITLE = "extra_article_title"

private const val GROUP_NEWS = "group_news"
private const val SUMMARY_ID = 1000










*/