package com.ola.fivethirtyeight.notification

enum class NotificationFrequency(val label: String, val minIntervalMs: Long) {
    IMMEDIATE("Immediately", 0L),
    THIRTY_MIN("Every 30 minutes", 30 * 60 * 1000L),
    HOURLY("Hourly", 60 * 60 * 1000L),
    DAILY("Daily", 24 * 60 * 60 * 1000L),
    NEVER("Never", Long.MAX_VALUE)
}