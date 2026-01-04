package com.ola.fivethirtyeight.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ola.fivethirtyeight.dataSource.NotificationDeduper
import com.ola.fivethirtyeight.datastore.SyncPreferences
import com.ola.fivethirtyeight.model.FeedKey
import com.ola.fivethirtyeight.notification.NotificationFrequency
import com.ola.fivethirtyeight.notification.isBreaking
import com.ola.fivethirtyeight.notification.showFeedNotification
import com.ola.fivethirtyeight.repository.TopStoriesFeedRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlin.coroutines.cancellation.CancellationException


@HiltWorker
class FeedSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val topRepo: TopStoriesFeedRepository,
    private val prefs: SyncPreferences,
    private val deduper: NotificationDeduper
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val now = System.currentTimeMillis()
        Log.d(TAG, "❤️ TopStories Worker heartbeat at $now")

        try {
            val notificationsEnabled = prefs.notificationsEnabledFlow.first()
            val frequency = prefs.notificationFrequencyFlow.first()

            // -------------------------------
            // 1. SYNC
            // -------------------------------
            val result = topRepo.syncTopStories()
            val newItems = result.newItems

            if (newItems.isEmpty()) return Result.success()

            // -------------------------------
            // 2. BREAKING NEWS
            // -------------------------------
            val breaking = newItems.filter { isBreaking(it, now) }

            if (breaking.isNotEmpty()) {
                val uniqueBreaking = breaking
                    .distinctBy { it.link }
                    .filterNot { deduper.isRecentlyNotified(it.link) }

                if (uniqueBreaking.isNotEmpty() && notificationsEnabled) {
                    showFeedNotification(uniqueBreaking, applicationContext)
                    deduper.markNotified(uniqueBreaking.map { it.link })
                    prefs.setLastNotified(FeedKey.TOP_STORIES.name, now)
                }

                // Continue to cadence for non-breaking items
            }

            // -------------------------------
            // 3. REGULAR CADENCE
            // -------------------------------
            val regular = newItems - breaking

            if (regular.isEmpty()) return Result.success()

            if (!notificationsEnabled || frequency == NotificationFrequency.NEVER)
                return Result.success()

            if (!shouldNotify(now, frequency))
                return Result.success()

            val uniqueRegular = regular
                .distinctBy { it.link }
                .filterNot { deduper.isRecentlyNotified(it.link) }

            if (uniqueRegular.isNotEmpty()) {
                showFeedNotification(uniqueRegular, applicationContext)
                deduper.markNotified(uniqueRegular.map { it.link })
                prefs.setLastNotified(FeedKey.TOP_STORIES.name, now)
            }

            return Result.success()

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "❌ Worker failed", e)
            return Result.retry()
        }
    }

    private suspend fun shouldNotify(
        now: Long,
        frequency: NotificationFrequency
    ): Boolean {
        val last = prefs.getLastNotified(FeedKey.TOP_STORIES.name)
        return now - last >= frequency.minIntervalMs
    }

    companion object {
        private const val TAG = "TopStoriesWorker"
    }
}


/*

@HiltWorker
class FeedSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val topRepo: TopStoriesFeedRepository,
    private val prefs: SyncPreferences,
    private val deduper: NotificationDeduper
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val now = System.currentTimeMillis()
        Log.d(TAG, "❤️ TopStories Worker heartbeat at $now")

        try {
            val notificationsEnabled = prefs.notificationsEnabledFlow.first()
            val frequency = prefs.notificationFrequencyFlow.first()

            // ---- 1) SYNC ----
            val result = topRepo.syncTopStories()
            val newItems = result.newItems

            if (newItems.isEmpty()) return Result.success()

            // ---- 2) BREAKING NEWS OVERRIDE ----
            val breaking = newItems.filter { isBreaking(it, now) }
            if (breaking.isNotEmpty()) {
                val uniqueBreaking = breaking
                    .distinctBy { it.link }
                    .filterNot { deduper.isRecentlyNotified(it.link) }

                if (uniqueBreaking.isNotEmpty() && notificationsEnabled) {
                    Log.d(TAG, "🚨 BREAKING NEWS override triggered")
                    showFeedNotification(uniqueBreaking, applicationContext)
                    deduper.markNotified(uniqueBreaking.map { it.link })
                    prefs.setLastNotified(FeedKey.TOP_STORIES.name, now)
                }

                // Still continue to regular cadence for non-breaking items
            }

            // ---- 3) REGULAR CADENCE ----
            val regular = newItems - breaking

            if (regular.isEmpty()) return Result.success()

            if (!notificationsEnabled || frequency == NotificationFrequency.NEVER)
                return Result.success()

            if (!shouldNotify(now, frequency))
                return Result.success()

            val uniqueRegular = regular
                .distinctBy { it.link }
                .filterNot { deduper.isRecentlyNotified(it.link) }

            if (uniqueRegular.isNotEmpty()) {
                showFeedNotification(uniqueRegular, applicationContext)
                deduper.markNotified(uniqueRegular.map { it.link })
                prefs.setLastNotified(FeedKey.TOP_STORIES.name, now)
            }

            return Result.success()

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "❌ Worker failed", e)
            return Result.retry()
        }
    }
*/

   /* private fun isBreaking(item: FeedItem, now: Long=System.currentTimeMillis()): Boolean {
        val published = item.timeInMil ?: return false
        return now - published <= BREAKING_NEWS_WINDOW_MS
    }*/
     /*fun isBreaking(item: FeedItem, now: Long): Boolean {
        val published = item.timeInMil ?: return false
        val priority = item.priority()
       return now - published <= priority.breakingWindowMs
    }*/



/*

    private suspend fun shouldNotify(now: Long, frequency: NotificationFrequency): Boolean {
        val last = prefs.getLastNotified(FeedKey.TOP_STORIES.name)
        return now - last >= frequency.minIntervalMs
    }

    companion object {
        private const val TAG = "TopStoriesWorker"
        private const val BREAKING_NEWS_WINDOW_MS = 7 * 60 * 1000L
    }
}
*/


/*

@HiltWorker
class FeedSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val topRepo: TopStoriesFeedRepository,
    private val prefs: SyncPreferences,
    private val deduper: NotificationDeduper
) : CoroutineWorker(context, params) {




    override suspend fun doWork(): Result {
        val notificationsEnabled = prefs.notificationsEnabledFlow.first()
        val now = System.currentTimeMillis()
        Log.d(TAG, "❤️ TopStories Worker heartbeat at $now")

        //Log.d("FeedSyncWorker", "🔕 notificationsEnabled=${prefs.notificationsEnabledFlow.first()}")
        //Log.d("FeedSyncWorker", "🔥 Worker started: ${System.currentTimeMillis()}")
        Log.d("FeedSyncWorker", "🚨 WORKER EXECUTED")
        Log.d(TAG, "🔕 notificationsEnabled=$notificationsEnabled")
        Log.d(TAG, "🔥 Worker started: ${System.currentTimeMillis()}")



        try {
            // Snapshot prefs once
            val notificationsEnabled = prefs.notificationsEnabledFlow.first()
            val frequency = prefs.notificationFrequencyFlow.first()

            // ---- 1) SYNC ----
            val result = topRepo.syncTopStories()
            val newItems = result.newItems

            Log.d(TAG, "📰 TopStories new=${newItems.size}")

            if (newItems.isEmpty()) {
                Log.d(TAG, "ℹ️ No new TopStories items")
                return Result.success()
            }

            // ---- 2) CADENCE ----
            if (!notificationsEnabled || frequency == NotificationFrequency.NEVER) {
                Log.d(TAG, "🔕 Notifications disabled — skipping notify")
                return Result.success()
            }

            if (!shouldNotify(now, frequency)) {
                Log.d(TAG, "⏱ Cadence blocked — skipping notify")
                return Result.success()
            }

            // ---- 3) DEDUPE ----
            val unique = newItems
                .distinctBy { it.link }
                .filterNot { deduper.isRecentlyNotified(it.link) }

            Log.d(TAG, "🔔 Unique TopStories items=${unique.size}")

            if (unique.isEmpty()) {
                Log.d(TAG, "ℹ️ All items already notified")
                return Result.success()
            }

            // ---- 4) NOTIFY ----
            showFeedNotification(unique, applicationContext)

            // Mark as notified
            deduper.markNotified(unique.map { it.link })
            prefs.setLastNotified(FeedKey.TOP_STORIES.name, now)

            Log.d(TAG, "✅ TopStories Worker finished successfully")
            return Result.success()

        } catch (e: CancellationException) {
            Log.d(TAG, "⏹ Worker cancelled")
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "❌ Worker failed", e)
            return Result.retry()
        }
    }

    private suspend fun shouldNotify(
        now: Long,
        frequency: NotificationFrequency
    ): Boolean {
        val last = prefs.getLastNotified(FeedKey.TOP_STORIES.name)
        val allowed = now - last >= frequency.minIntervalMs

        Log.d(
            TAG,
            "⏱ shouldNotify last=$last now=$now allowed=$allowed"
        )

        return allowed
    }




    companion object {
        private const val TAG = "TopStoriesWorker"
        const val BREAKING_NEWS_WINDOW_MS = 5 * 60 * 1000L // 5 minutes
    }



}
*/


/*

@HiltWorker
class FeedSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val topRepo: TopStoriesFeedRepository,
    private val politicsRepo: PoliticsFeedRepository,
    private val worldRepo: WorldFeedRepository,
    private val businessRepo: BusinessFeedRepository,
    private val techRepo: TechFeedRepository,
    private val sportsRepo: SportsFeedRepository,
    private val healthRepo: HealthFeedRepository,
    private val fiveThirtyEightRepo: FiveThirtyEightFeedRepository,
    private val prefs: SyncPreferences,
    private val deduper: NotificationDeduper
) : CoroutineWorker(context, params) {



    override suspend fun doWork(): Result {
        try {
            val notificationsEnabled = prefs.notificationsEnabledFlow.first()
            //Log.d("FeedSyncWorker", "🔕 notificationsEnabled=${prefs.notificationsEnabledFlow.first()}")
            //Log.d("FeedSyncWorker", "🔥 Worker started: ${System.currentTimeMillis()}")
            Log.d("FeedSyncWorker", "🚨 WORKER EXECUTED")
            Log.d(TAG, "🔕 notificationsEnabled=$notificationsEnabled")
            Log.d(TAG, "🔥 Worker started: ${System.currentTimeMillis()}")

            val now = System.currentTimeMillis()
            val collected = mutableListOf<FeedItem>()

            // -------- TOP STORIES --------
            val topResult = topRepo.syncTopStories()
            Log.d(TAG, "📰 TopStories new=${topResult.newItems.size}")

            if (
                topResult.newItems.isNotEmpty() &&
                shouldNotify(FeedKey.TOP_STORIES.name, now)
            ) {
                collected += topResult.newItems
                prefs.setLastNotified(FeedKey.TOP_STORIES.name, now)
            }

            // -------- DEDUP & NOTIFY --------
            val unique = collected
                .distinctBy { it.link }
                .filterNot { deduper.isRecentlyNotified(it.link) }

            Log.d(TAG, "🔔 Unique notify items=${unique.size}")

            if (unique.isNotEmpty()) {
                showFeedNotification(unique, applicationContext)
                deduper.markNotified(unique.map { it.link })
            }

            Log.d(TAG, "✅ FeedSyncWorker finished successfully")
            return Result.success()

        } catch (e: CancellationException) {
            Log.d(TAG, "⏹ FeedSyncWorker cancelled")
            throw e // 🔥 REQUIRED — do NOT swallow cancellation
        } catch (e: Exception) {
            Log.e(TAG, "❌ FeedSyncWorker failed", e)
            return Result.retry()
        }
    }

    */
/**
     * Per-feed throttle (news-style)
     *//*

    private suspend fun shouldNotify(
        feed: String,
        now: Long,

    ): Boolean {
// 🔔 Read user's preference (ONE snapshot per worker run)

        // 🔕 GLOBAL notification toggle
        val notificationsEnabled =
            prefs.notificationsEnabledFlow.first()

        if (!notificationsEnabled) {
            Log.d(TAG, "🔕 Notifications disabled by user")
            return false
        }



        val frequency = prefs.notificationFrequencyFlow.first()

        // User explicitly disabled notifications
        if (frequency == NotificationFrequency.NEVER) {
            Log.d(TAG, "🔕 Notifications disabled by user")
            return false
        }

        val last = prefs.getLastNotified(feed)
        val allowed = now - last >= frequency.minIntervalMs

        Log.d(
            TAG,
            "🔔 shouldNotify feed=$feed freq=${frequency.name} " +
                    "last=$last now=$now allowed=$allowed"
        )

        return allowed

    }


    companion object {
        private const val TAG = "FeedSyncWorker"
    }
}

*/










/*

override suspend fun doWork(): ListenableWorker.Result {
    try {
        Log.d(TAG, "🚨 WORKER EXECUTED")

        val now = System.currentTimeMillis()

        // ✅ snapshot prefs ONCE
        val notificationsEnabled = prefs.notificationsEnabledFlow.first()
        val frequency = prefs.notificationFrequencyFlow.first()
        if (!notificationsEnabled || frequency == NotificationFrequency.NEVER) {
            Log.d(TAG, "🔕 Notifications disabled")
            return ListenableWorker.Result.success()
        }

        // ✅ sync feeds in parallel
        val results = kotlinx.coroutines.supervisorScope {
            listOf(
                async { FeedKey.TOP_STORIES to topRepo.syncTopStories() },
                async { FeedKey.POLITICS to politicsRepo.syncPolitics() },
                async { FeedKey.WORLD to worldRepo.syncWorld() },
                async { FeedKey.BUSINESS to businessRepo.syncBusiness() },
                async { FeedKey.TECH to techRepo.syncTech() },
                async { FeedKey.SPORTS to sportsRepo.syncSports() },
                async { FeedKey.HEALTH to healthRepo.syncHealth() },
                async { FeedKey.FIVETHIRTYEIGHT to fiveThirtyEightRepo.syncFiveThirtyEight() }
            ).awaitAll()
        }

        val collected = mutableListOf<FeedItem>()

        for ((key, res) in results) {
            Log.d(TAG, "📰 ${key.name} new=${res.newItems.size}")

            val allowed = shouldNotifySnapshot(
                feed = key.name,
                now = now,
                frequency = frequency
            )
            if (res.newItems.isNotEmpty() && allowed) {
                collected += res.newItems
                prefs.setLastNotified(key.name, now)
            }
        }

        val unique = collected
            .distinctBy { it.link }
            .filterNot { deduper.isRecentlyNotified(it.link) }

        if (unique.isNotEmpty()) {
            showFeedNotification(unique, applicationContext)
            deduper.markNotified(unique.map { it.link })
        }

        Log.d(TAG, "✅ FeedSyncWorker finished successfully")
        return ListenableWorker.Result.success()

    } catch (e: CancellationException) {
        Log.d(TAG, "⏹ FeedSyncWorker cancelled")
        throw e
    } catch (e: Exception) {
        Log.e(TAG, "❌ FeedSyncWorker failed", e)
        return ListenableWorker.Result.retry()
    }
}

private suspend fun shouldNotifySnapshot(
    feed: String,
    now: Long,
    frequency: NotificationFrequency
): Boolean {
    val last = prefs.getLastNotified(feed)
    return now - last >= frequency.minIntervalMs
}

*/












