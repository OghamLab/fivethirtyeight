package com.ola.fivethirtyeight.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.ola.fivethirtyeight.notification.NotificationFrequency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SyncPreferences @Inject constructor(private val dataStore: DataStore<Preferences>) {
    companion object {
        val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
        fun lastNotifiedKey(feed: String) =
            longPreferencesKey("last_notified_$feed")
    }


    suspend fun getLastNotified(feed: String): Long =
        dataStore.data.first()[lastNotifiedKey(feed)] ?: 0L

    suspend fun setLastNotified(feed: String, time: Long) {
        dataStore.edit { it[lastNotifiedKey(feed)] = time }
    }


    suspend fun getLastSyncTime(): Long {
        return dataStore.data.first()[LAST_SYNC_TIME] ?: 0L
    }

    suspend fun setLastSyncTime(timestamp: Long) {
        dataStore.edit { it[LAST_SYNC_TIME] = timestamp }
    }





    val notificationsEnabledFlow: Flow<Boolean> =
        dataStore.data.map {
            it[SettingsKeys.NOTIFICATIONS] ?: true
        }


    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[SettingsKeys.NOTIFICATIONS] = enabled }
    }


    suspend fun setThemePref(pref: String) {
        dataStore.edit { it[SettingsKeys.THEME_PREF] = pref }
    }

    suspend fun setNotificationFrequency(freq: NotificationFrequency) {
        dataStore.edit { it[SettingsKeys.NOTIFICATION_FREQUENCY] = freq.name }
    }

    val themePrefFlow: Flow<String> =
        dataStore.data.map { it[SettingsKeys.THEME_PREF] ?: "System Default" }

    val notificationFrequencyFlow: Flow<NotificationFrequency> = dataStore.data.map {
        it[SettingsKeys.NOTIFICATION_FREQUENCY]?.let(NotificationFrequency::valueOf)
            ?: NotificationFrequency.HOURLY
    }


}
