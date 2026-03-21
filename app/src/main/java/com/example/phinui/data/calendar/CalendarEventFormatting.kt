package com.example.phinui.data.calendar

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

// Formatter for date-only strings like "2026-03-20"
private val localDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

// Returns the LocalDate that an event belongs to.

fun eventDate(event: CalendarEvent): LocalDate? {
    return when (event.source) {
        CalendarSource.GOOGLE -> googleEventDate(event)
        CalendarSource.LOCAL -> localEventDate(event)
    }
}

// Extracts the date from a Google Calendar event.
private fun googleEventDate(event: CalendarEvent): LocalDate? {
    val start = event.start
    if (start.isBlank()) return null

    return try {
        if (start.contains("T")) {
            OffsetDateTime.parse(start).toLocalDate()
        } else {
            LocalDate.parse(start, localDateFormatter)
        }
    } catch (_: Exception) {
        null
    }
}

// Extracts the date from a locally stored event.
private fun localEventDate(event: CalendarEvent): LocalDate? {
    val start = event.start
    if (start.isBlank()) return null

    return try {
        if (start.contains("T")) {
            LocalDateTime.parse(start).toLocalDate()
        } else {
            LocalDate.parse(start, localDateFormatter)
        }
    } catch (_: Exception) {
        null
    }
}

// Returns the formatted time string shown in the UI for an event.
fun formatEventTimeLine(event: CalendarEvent): String {
    return when (event.source) {
        CalendarSource.GOOGLE -> formatGoogleEventTimeLine(event)
        CalendarSource.LOCAL -> formatLocalEventTimeLine(event)
    }
}

// Formats the time range for a Google event.
private fun formatGoogleEventTimeLine(event: CalendarEvent): String {
    val start = event.start
    val end = event.end

    if (start.isBlank()) return ""

    if (!start.contains("T")) return "All day"

    val startTime = extractGoogleDisplayTime(start)
    val endTime = if (end.isNotBlank() && end.contains("T")) extractGoogleDisplayTime(end) else ""

    return when {
        startTime.isNotBlank() && endTime.isNotBlank() -> "$startTime - $endTime"
        startTime.isNotBlank() -> startTime
        else -> ""
    }
}

// Formats the time range for a locally stored event.
private fun formatLocalEventTimeLine(event: CalendarEvent): String {
    val start = event.start
    val end = event.end

    if (start.isBlank()) return ""

    if (!start.contains("T")) return "All day"

    val startTime = extractLocalDisplayTime(start)
    val endTime = if (end.isNotBlank() && end.contains("T")) extractLocalDisplayTime(end) else ""

    return when {
        startTime.isNotBlank() && endTime.isNotBlank() -> "$startTime - $endTime"
        startTime.isNotBlank() -> startTime
        else -> ""
    }
}

// Converts a Google datetime string into a user-friendly 12-hour time.
private fun extractGoogleDisplayTime(iso: String): String {
    return try {
        val dateTime = OffsetDateTime.parse(iso)
        formatHourMinute(dateTime.hour, dateTime.minute)
    } catch (_: Exception) {
        ""
    }
}

// Converts a local datetime string into a user-friendly 12-hour time.
private fun extractLocalDisplayTime(dateTimeText: String): String {
    return try {
        val dateTime = LocalDateTime.parse(dateTimeText)
        formatHourMinute(dateTime.hour, dateTime.minute)
    } catch (_: Exception) {
        ""
    }
}

// Shared helper that converts 24-hour hour/minute values
private fun formatHourMinute(hour: Int, minute: Int): String {
    val amPm = if (hour < 12) "AM" else "PM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }

    val minuteText = minute.toString().padStart(2, '0')
    return "$displayHour:$minuteText $amPm"
}

// Converts reminder minutes into readable text.

fun formatReminderText(reminders: List<Int>): String? {
    if (reminders.isEmpty()) return null

    return reminders
        .distinct()
        .sorted()
        .joinToString(", ") { minutes ->
            when {
                minutes < 60 -> "$minutes min before"
                minutes % 60 == 0 && minutes < 1440 -> "${minutes / 60} hr before"
                minutes == 1440 -> "1 day before"
                minutes % 1440 == 0 -> "${minutes / 1440} days before"
                else -> "$minutes min before"
            }
        }
}