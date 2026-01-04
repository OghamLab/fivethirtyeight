package com.ola.fivethirtyeight.utils

import android.text.format.DateUtils
import androidx.compose.runtime.Composable
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun String.toRelativeTime(): String {
    return try {
        val sdf = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH)
        val time = sdf.parse(this)?.time ?: 0L
        val now = System.currentTimeMillis()
        DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS).toString()
    } catch (e: Exception) {
        "Unknown time"
    }

}

