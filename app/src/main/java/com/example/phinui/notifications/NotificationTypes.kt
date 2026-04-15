package com.example.phinui.notifications

import android.app.NotificationManager

enum class NotificationType(
    val channelId: String,
    val channelName: String,
    val importance: Int,
) {

    CALENDAR(
        "calendar_reminders",
        "Calendar Reminders V2",
        NotificationManager.IMPORTANCE_HIGH
    ),

    FRIEND_REQUESTS(
        "friend_request_channel",
        "Friend Requests V2",
        NotificationManager.IMPORTANCE_HIGH
    )
}