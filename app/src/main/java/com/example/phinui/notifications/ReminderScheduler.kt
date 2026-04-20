package com.example.phinui.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.phinui.data.calendar.CalendarEvent
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import android.app.AlarmManager
import android.util.Log
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

class ReminderScheduler(private val context: Context) {

    // to keep track of scheduled reminders per eventId
    private val remindersMap = mutableMapOf<String, List<Int>>()
    fun scheduleReminder(event: CalendarEvent) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w(
                    "ReminderScheduler",
                    "App cannot schedule exact alarms. Ask user to allow in settings."
                )
                return
            }
        }

        // cancel any existing jobs for the event
        cancelReminder(event.id)

        // save reminder minutes
        remindersMap[event.id] = event.reminderMinutes

        // loop through all reminders for the event
        event.reminderMinutes.forEach { minutesBefore ->

            // convert event.start to Instant
            val eventStart = try {
                OffsetDateTime.parse(event.start).toInstant()
            } catch (e: DateTimeParseException) {
                try {
                    LocalDateTime.parse(event.start)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                } catch (e: DateTimeParseException) {
                    LocalDate.parse(event.start)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
                }
            }

            val triggerTime = eventStart
                .minus(minutesBefore.toLong(), ChronoUnit.MINUTES)

            val delay = Duration
                .between(Instant.now(), triggerTime)
                .toMillis()

            // skip reminders in the past
            if (delay <= 0) return@forEach

            val intent = Intent(context, ReminderReceiver::class.java).apply {
                action = "com.example.PhinUI.REMINDER"
                putExtra("title", event.title)
                putExtra("eventId", event.id)
            }

            val requestCode = (event.id.hashCode() * 31) + minutesBefore

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime.toEpochMilli(),
                pendingIntent
            )
        }
    }

    fun cancelReminder(eventId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // get reminder minutes for the event
        val reminderMinutes = remindersMap[eventId] ?: return

        reminderMinutes.forEach { minutesBefore ->
            val requestCode = (eventId.hashCode() * 31) + minutesBefore

            val intent = Intent(context, ReminderReceiver::class.java).apply {
                action = "com.example.PhinUI.REMINDER"
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )

            pendingIntent?.let {
                alarmManager.cancel(it)
                it.cancel()
            }
        }

        // remove from map
        remindersMap.remove(eventId)
    }
}