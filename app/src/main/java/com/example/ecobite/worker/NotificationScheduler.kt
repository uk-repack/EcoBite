package com.example.ecobite.worker

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.ecobite.data.local.entities.PantryItem
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    private val context: Context
) {
    fun scheduleExpiryNotification(item: PantryItem) {
        val now         = System.currentTimeMillis()
        val expiryMs    = item.expiryDate
        val notifyAtMs  = expiryMs - TimeUnit.HOURS.toMillis(48)
        val delayMs     = notifyAtMs - now

        // only schedule if expiry is more than 48 hours away
        if (delayMs <= 0) return

        val daysLeft = TimeUnit.MILLISECONDS.toDays(expiryMs - now)

        val inputData = Data.Builder()
            .putString(ExpiryNotificationWorker.KEY_ITEM_NAME, item.name)
            .putLong(ExpiryNotificationWorker.KEY_DAYS_LEFT, daysLeft)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<ExpiryNotificationWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag("expiry_${item.id}")
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "expiry_${item.id}",
                androidx.work.ExistingWorkPolicy.REPLACE,
                workRequest
            )
    }

    fun cancelNotification(itemId: Int) {
        WorkManager.getInstance(context)
            .cancelUniqueWork("expiry_${itemId}")
    }
}