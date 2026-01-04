package com.ola.fivethirtyeight.viewmodel
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.ola.fivethirtyeight.datastore.SettingsState
import com.ola.fivethirtyeight.datastore.SyncPreferences
import com.ola.fivethirtyeight.feedSyncScheduler.FeedSyncScheduler
import com.ola.fivethirtyeight.feedSyncScheduler.FeedSyncWorkIds
import com.ola.fivethirtyeight.notification.NotificationFrequency
import com.ola.fivethirtyeight.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(application: Application,
                                            private val repo: SettingsRepository,
                                            private val prefs: SyncPreferences
) : AndroidViewModel(application) {




    val settingsState: StateFlow<SettingsState> =
        repo.settingsFlow.stateIn(viewModelScope, SharingStarted.Eagerly, SettingsState())

    /*fun toggleNotifications(enabled: Boolean) = viewModelScope.launch {
        repo.updateNotifications(enabled)

        Log.d("Settings", "Notifications toggled: $enabled")
    }*/



   /* fun setThemePref(themePref: String) = viewModelScope.launch {
        repo.updateThemePref(themePref)
    }

    fun setNotificationFrequency(freq: NotificationFrequency) {
        viewModelScope.launch {
            repo.updateNotificationFrequency(freq)
        }
    }*/

    fun setThemePref(pref: String) = viewModelScope.launch {
        prefs.setThemePref(pref)
    }

    fun setNotificationFrequency(freq: NotificationFrequency) = viewModelScope.launch {
        prefs.setNotificationFrequency(freq)
    }








    /*fun toggleNotifications(enabled: Boolean) = viewModelScope.launch {
        prefs.setNotificationsEnabled(enabled)
        Log.d("Settings", "Notifications toggled: $enabled")
    }*/


    fun toggleNotifications(enabled: Boolean) = viewModelScope.launch {
        prefs.setNotificationsEnabled(enabled)

        val wm = WorkManager.getInstance(getApplication())

        if (!enabled) {
            wm.cancelUniqueWork(FeedSyncWorkIds.IMMEDIATE)
            wm.cancelUniqueWork(FeedSyncWorkIds.BACKGROUND)
            wm.cancelUniqueWork(FeedSyncWorkIds.IDLE)
        } else {
            FeedSyncScheduler.scheduleBackground(getApplication())
            FeedSyncScheduler.scheduleIdle(getApplication())
        }

        Log.d("not", "Notifications toggled: $enabled")
    }





}
