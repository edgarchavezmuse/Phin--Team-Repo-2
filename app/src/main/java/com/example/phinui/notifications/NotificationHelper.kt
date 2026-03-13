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

object NotificationHelper {

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
        }
        else {
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