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

class ReminderScheduler(private val context: Context) {

    fun scheduleReminder(event: CalendarEvent) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w("ReminderScheduler", "App cannot schedule exact alarms. Ask user to allow in settings.")
                return
            }
        }

        // cancel any existing jobs for the event
        cancelReminder(event.id)

        // loop through all reminders for the event
        event.reminderMinutes.forEach { minutesBefore ->

            // convert event.start to Instant
            val eventStart = if (event.start.contains('T')) {
                OffsetDateTime.parse(event.start).toInstant()
            } else {
                LocalDate.parse(event.start).atStartOfDay(ZoneId.systemDefault()).toInstant()
            }

            val triggerTime = eventStart
                .minus(minutesBefore.toLong(), ChronoUnit.MINUTES)

            val delay = Duration
                .between(Instant.now(), triggerTime)
                .toMillis()

            // skip reminders in the past
            if (delay <= 0) return@forEach

            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra("title", event.title)
                putExtra("eventId", event.id)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                "$event-${minutesBefore}".hashCode(),
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
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE)

    }
}