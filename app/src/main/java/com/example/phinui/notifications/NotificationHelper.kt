package com.example.phinui.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.phinui.MainActivity
import com.example.phinui.R

object NotificationHelper {

    // function specifically for dealing with notifications involving google calendar
    fun sendNotificationsForCalendar(
        context: Context,
        title: String?,
        eventId: String,
    ) {

        // need to make sure to update this whenever editing notification build
        val channelId = "reminders_v3"

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )

            manager.createNotificationChannel(channel)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentTitle =
            if (title == null) {
                "You have an event coming up!"
            } else {
                "Upcoming Event"
            }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(contentTitle)
            .setContentText(title)
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
            Log.d("NotifDebug", "Permission not granted to send notifications")
            return
        }

        NotificationManagerCompat.from(context)
            .notify(eventId.hashCode(), notification)

    }

    // functions for potential future use of other notification types

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

    fun sendNotification(
        context: Context,
        type: NotificationType,
        customTitle: String? = null,
        customMessage: String? = null
    ) {

        val title = customTitle ?: type.title
        val message = customMessage ?: type.message

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("notificationType", type.name)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            type.ordinal,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, type.channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("NotifDebug", "Permission not granted to send notifications")
            return
        }

        NotificationManagerCompat.from(context)
            .notify(type.ordinal, builder.build())
    }
}