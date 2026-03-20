package com.example.phinui.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.phinui.notifications.NotificationHelper.sendNotificationsForCalendar

// works with Alarm Manager to allow for notifications even when app is closed
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: return
        val eventId = intent.getStringExtra("eventId") ?: return

        sendNotificationsForCalendar(context, title, eventId)
    }
}