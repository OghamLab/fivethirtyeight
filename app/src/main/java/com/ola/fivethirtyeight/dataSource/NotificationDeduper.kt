package com.ola.fivethirtyeight.dataSource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class NotificationDeduper @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val recent = stringPreferencesKey("recent_links")

    suspend fun isRecentlyNotified(
        link: String,
        windowMs: Long = 6 * 60 * 60 * 1000L
    ): Boolean {
        val now = System.currentTimeMillis()
        val map = dataStore.data.first()[recent]?.split("|") ?: return false

        return map.any {
            val parts = it.split(":")
            if (parts.size == 2) {
                val (l, t) = parts
                l == link && now - t.toLong() < windowMs
            } else {
                false
            }
        }
    }

    /*suspend fun markNotified(links: List<String>) {
        val now = System.currentTimeMillis()
        val value = links.joinToString("|") { "$it:$now" }
        dataStore.edit { it[recent] = value }
    }*/


    suspend fun markNotified(links: List<String>) {
        val now = System.currentTimeMillis()

        dataStore.edit { prefs ->
            val existing = prefs[recent]
                ?.split("|")
                ?.associate {
                    val (l, t) = it.split(":")
                    l to t
                }
                ?: emptyMap()

            val updated = existing.toMutableMap()
            links.forEach { updated[it] = now.toString() }

            prefs[recent] = updated.entries.joinToString("|") {
                "${it.key}:${it.value}"
            }
        }
    }


}
