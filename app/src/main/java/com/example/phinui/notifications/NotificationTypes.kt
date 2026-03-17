package com.example.phinui.notifications

import android.app.NotificationManager

enum class NotificationType(
    val channelId: String,
    val channelName: String,
    val importance: Int,
    val title: String,
    val message: String
) {

    SCHEDULE(
        "schedule",
        "Schedule",
        NotificationManager.IMPORTANCE_HIGH,
        "Schedule Reminder",
        "Your class will start soon!"
    ),

    ASSIGNMENT(
        "assignment",
        "Assignment",
        NotificationManager.IMPORTANCE_HIGH,
        "Assignment Due",
        "You have an assignment due soon!"
    ),

    EVENT(
        "event",
        "Event",
        NotificationManager.IMPORTANCE_HIGH,
        "Event Reminder",
        "There is an event coming up!"
    )
}