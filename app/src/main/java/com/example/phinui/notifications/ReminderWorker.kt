package com.example.phinui.notifications

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {

        val typeName = inputData.getString("notification_type")
            ?: return Result.failure()

        val notificationType = NotificationType.valueOf(typeName)

        val title = inputData.getString("title")
        val message = inputData.getString("message")

        NotificationHelper.sendNotification(
            context = applicationContext,
            type = notificationType,
            customTitle = title,
            customMessage = message
        )

        return Result.success()

    }
}