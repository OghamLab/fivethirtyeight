package com.ola.fivethirtyeight.repository

import com.ola.fivethirtyeight.datastore.SettingsState
import com.ola.fivethirtyeight.datastore.SyncPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject


class SettingsRepository @Inject constructor(private val prefs: SyncPreferences) {


    val settingsFlow: Flow<SettingsState> = combine(
        prefs.notificationsEnabledFlow,
        prefs.notificationFrequencyFlow,
        prefs.themePrefFlow
    ) { notifications, frequency, theme ->
        SettingsState(
            notifications = notifications,
            notificationFrequency = frequency,
            themePref = theme
        )
    }


    /*suspend fun updateNotifications(enabled: Boolean) {
        dataStore.edit { it[SettingsKeys.NOTIFICATIONS] = enabled }
    }
*/


    /*suspend fun updateThemePref(pref: String) {
        dataStore.edit { it[SettingsKeys.THEME_PREF] = pref }
    }

    suspend fun updateNotificationFrequency(freq: NotificationFrequency) {
        dataStore.edit {
            it[SettingsKeys.NOTIFICATION_FREQUENCY] = freq.name
        }
    }*/


}
