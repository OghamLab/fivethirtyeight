package com.ola.fivethirtyeight.feedSyncScheduler

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ola.fivethirtyeight.worker.FeedSyncWorker
import java.util.concurrent.TimeUnit


//Only schedule workers
//👉 Never sync data
//👉 Never touch UI

object FeedSyncScheduler {


   private fun baseConstraints(
       requireCharging: Boolean = false
   ): Constraints =
       Constraints.Builder()
           .setRequiredNetworkType(NetworkType.CONNECTED)
           .apply {
               if (requireCharging) setRequiresCharging(true)
           }
           .build()


    /** One-time sync (app open, manual trigger, debug) */
    fun triggerImmediate(context: Context) {
        val request = OneTimeWorkRequestBuilder<FeedSyncWorker>()
            .setConstraints(baseConstraints())
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                FeedSyncWorkIds.IMMEDIATE,
                ExistingWorkPolicy.KEEP,
                request
            )

    }



    fun scheduleBackground(context: Context) {
        val work = PeriodicWorkRequestBuilder<FeedSyncWorker>(
            30, TimeUnit.MINUTES
        )
            .setConstraints(baseConstraints())
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                FeedSyncWorkIds.BACKGROUND,
                ExistingPeriodicWorkPolicy.UPDATE,
                work
            )
    }





    fun scheduleIdle(context: Context) {
        val work = PeriodicWorkRequestBuilder<FeedSyncWorker>(
            4, TimeUnit.HOURS
        )
            .setConstraints(baseConstraints(requireCharging = true))
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                FeedSyncWorkIds.IDLE,
                ExistingPeriodicWorkPolicy.UPDATE,
                work
            )

    }


}


object FeedSyncWorkIds {
    const val IMMEDIATE = "feed_sync_immediate"
    const val BACKGROUND = "feed_sync_background"
    const val IDLE = "feed_sync_idle"
}
