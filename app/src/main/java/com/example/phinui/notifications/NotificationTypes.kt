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
        "Friend Requests V3",
        NotificationManager.IMPORTANCE_HIGH
    ),

    MESSAGES(
        "message_channel",
        "Messages V1",
        NotificationManager.IMPORTANCE_HIGH
    )
}