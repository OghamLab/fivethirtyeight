package com.ola.fivethirtyeight.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ola.fivethirtyeight.notification.NotificationFrequency


data class SettingsState(
    val notifications: Boolean = true,
    val darkMode: Boolean = false,
    val themePref: String = "System Default",
    val notificationFrequency: NotificationFrequency = NotificationFrequency.HOURLY,

    )



object SettingsKeys {
    val NOTIFICATIONS = booleanPreferencesKey("notifications")
    val DARK_MODE = booleanPreferencesKey("dark_mode")
    val THEME_PREF = stringPreferencesKey("theme_pref")

    // 🆕
    val NOTIFICATION_FREQUENCY =
        stringPreferencesKey("notification_frequency")


}

















/*ta class SettingsState(
    val darkMode: Boolean,
    val notifications: Boolean,
    val themePref: String,
    val fontScale: Float // e.g., 1.0f = default, 1.2f = larger
)


object SettingsKeys {
    val FONT_SCALE = floatPreferencesKey("font_scale")
    val DARK_MODE = booleanPreferencesKey("dark_mode")
    val NOTIFICATIONS = booleanPreferencesKey("notifications")
    val THEME_PREF = stringPreferencesKey("theme_pref")
}

class DataStoreManager(private val context: Context) {

    private val dataStore = context.settingsDataStore

    val settingsFlow: Flow<SettingsState> = dataStore.data.map { prefs ->
        SettingsState(
            darkMode = prefs[SettingsKeys.DARK_MODE] ?: false,
            notifications = prefs[SettingsKeys.NOTIFICATIONS] ?: false,
            themePref = prefs[SettingsKeys.THEME_PREF] ?: "default",
            fontScale = prefs[SettingsKeys.FONT_SCALE] ?: 1.0f) }


    suspend fun updateFontScale(scale: Float) {
      dataStore.edit {
          it[SettingsKeys.FONT_SCALE] = scale



      }
    }


*/

