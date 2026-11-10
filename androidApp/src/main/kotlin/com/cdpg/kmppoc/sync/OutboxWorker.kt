package com.cdpg.kmppoc.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.cdpg.kmppoc.DhruvaApplication
import java.util.concurrent.TimeUnit

/**
 * Android's trigger for the shared outbox: WorkManager runs it when the network is back.
 * The replay logic itself (order, idempotency keys, what to drop) is in :shared OutboxProcessor;
 * iOS triggers the same code on app foreground.
 */
class OutboxWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val graph = (applicationContext as DhruvaApplication).graph
        graph.drainOutbox()
        return Result.success()
    }

    companion object {
        private val ONLINE = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        /** Right after a write was queued: runs as soon as there is a connection. */
        fun enqueueNow(context: Context) {
            WorkManager.getInstance(context).enqueueUniqueWork(
                "outbox-now",
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<OutboxWorker>().setConstraints(ONLINE).build(),
            )
        }

        /** Safety net in case a one-off run was missed. */
        fun schedulePeriodic(context: Context) {
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "outbox-periodic",
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<OutboxWorker>(6, TimeUnit.HOURS).setConstraints(ONLINE).build(),
            )
        }
    }
}
