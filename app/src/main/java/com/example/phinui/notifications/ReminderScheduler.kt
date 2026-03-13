package com.example.phinui.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    fun scheduleReminder(
        context: Context,
        reminderId: String,
        type: NotificationType,
        triggerTimeMillis: Long,
        customTitle: String? = null,
        customMessage: String? = null
    ) {

        val delay = triggerTimeMillis - System.currentTimeMillis()
        if (delay <= 0) return

        val data = Data.Builder()
            .putString("notification_type", type.name)
            .apply {
                customTitle?.let { putString("title", it) }
                customMessage?.let { putString("message", it) }
            }
            .build()

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            reminderId,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancelReminder(context: Context, reminderId: String) {
        WorkManager.getInstance(context).cancelUniqueWork(reminderId)
    }
}