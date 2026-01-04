package com.ola.fivethirtyeight.application

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ola.fivethirtyeight.feedSyncScheduler.FeedSyncScheduler
import com.ola.fivethirtyeight.worker.FeedSyncWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@HiltAndroidApp
class FiveThirtyEightApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory



    override fun onCreate() {
        super.onCreate()
        scheduleFeedSync(this)
        FeedSyncScheduler.triggerImmediate(applicationContext)
        FeedSyncScheduler.scheduleBackground(applicationContext)
        FeedSyncScheduler.scheduleIdle(applicationContext)


        val breakingChannel = NotificationChannel(
            "breaking_news",
            "Breaking News",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Urgent breaking news alerts"
            enableLights(true)
            lightColor = android.graphics.Color.RED
            enableVibration(true)
        }

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(breakingChannel)


    }

    private fun scheduleFeedSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val work = PeriodicWorkRequestBuilder<FeedSyncWorker>(
            30, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "feed_sync",
                ExistingPeriodicWorkPolicy.UPDATE,
                work
            )
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder() .setWorkerFactory(workerFactory) .setMinimumLoggingLevel(
        Log.DEBUG) .build()




}

