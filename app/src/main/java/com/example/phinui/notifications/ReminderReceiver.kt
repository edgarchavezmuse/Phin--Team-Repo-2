package com.example.phinui.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.phinui.notifications.NotificationHelper.sendNotificationsForCalendar

// works with Alarm Manager to allow for notifications even when app is closed
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ReminderTitleCheck", "intent.getStringExtra value: " + intent.getStringExtra("title"))

        val titleTester = intent.getStringExtra("title")
        val title =
            if (titleTester != null) {
                if (titleTester == "(No title)") {
                    null
                } else {
                    titleTester
                }
            } else {
                return
            }

        val eventId = intent.getStringExtra("eventId") ?: return

        sendNotificationsForCalendar(context, title, eventId)
    }
}