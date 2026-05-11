package com.example.ecobite.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ExpiryNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val itemName = inputData.getString(KEY_ITEM_NAME) ?: return Result.failure()
        val daysLeft = inputData.getLong(KEY_DAYS_LEFT, 0)

        createNotificationChannel()

        val message = when {
            daysLeft <= 0L -> "$itemName has expired. Log it as waste or check if it's still good."
            daysLeft == 1L -> "$itemName expires tomorrow. Cook it today to avoid waste."
            else           -> "$itemName expires in $daysLeft days. Plan a meal around it."
        }

        val notification = NotificationCompat.Builder(
            applicationContext, CHANNEL_ID
        )
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("EcoBite — Expiry Reminder 🌱")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(itemName.hashCode(), notification)

        return Result.success()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Expiry Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for food items expiring soon"
        }
        val manager = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID    = "expiry_reminders"
        const val KEY_ITEM_NAME = "item_name"
        const val KEY_DAYS_LEFT = "days_left"
    }
}