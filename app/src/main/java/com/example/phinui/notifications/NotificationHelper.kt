package com.example.phinui.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.phinui.R

object NotificationHelper {

    fun sendNotificationsForCalendar(
        context: Context,
        title: String?,
        eventId: String,
    ) {

        val intent =
            Intent(Intent.ACTION_VIEW, Uri.parse("phin://calendar")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

        val contentTitle =
            if (title == null) {
                "You have an event coming up!"
            } else {
                "Upcoming Event"
            }

        showNotification(
            context = context,
            type = NotificationType.CALENDAR,
            title = contentTitle,
            body = title,
            intent = intent,
            requestCode = eventId.hashCode()
        )
    }

    fun createNotificationChannels(context: Context) {
        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationType.entries.forEach { type ->
                val channel = NotificationChannel(
                    type.channelId,
                    type.channelName,
                    type.importance
                )

                manager.createNotificationChannel(channel)
            }
        } else {
            Log.d("NotifDebug", "Unable to create notification channels.")
        }
    }

    fun showNotification(
        context: Context,
        type: NotificationType,
        title: String?,
        body: String?,
        intent: Intent,
        requestCode: Int = 0
    ) {
        val pendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, type.channelId)
            .setContentTitle(title ?: type.channelName)
            .setContentText(body)
            .setSmallIcon(R.drawable.redphin)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("NotifDebug", "Notification permission not granted")
            return
        }

        NotificationManagerCompat.from(context)
            .notify(requestCode, notification)
    }
}