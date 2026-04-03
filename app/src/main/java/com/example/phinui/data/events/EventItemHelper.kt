package com.example.phinui.data.events

import com.example.phinui.data.calendar.CalendarEvent
import com.example.phinui.data.calendar.CalendarSource
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

fun EventItem.toCalendarEvent(): CalendarEvent {

    val startEnd = parseRssDate(description)
    val location = extractLocation(description)

    return CalendarEvent(
        id = link,
        title = title,
        start = startEnd.first,
        end = startEnd.second,
        location = location,
        reminderMinutes = emptyList(),
        source = CalendarSource.LOCAL
    )
}

fun String.cleanHtml(): String {
    return this.replace("&nbsp;", " ")
        .replace("<br/>", "\n")
        .replace("<br>", "\n")
        .trim()
}


fun parseRssDate(description: String): Pair<String, String> {
    val clean = description.cleanHtml()
        .replace("&nbsp;", " ")
        .replace("&ndash;", "-")
        .replace("<br/", "\n")
        .replace("<br>", "\n")
        .trim()

    // split lines
    val dateTimeLine = clean.lines()
        .firstOrNull() { it.contains(Regex("\\b[A-Za-z]+ \\d{1,2}\\b")) }
        ?: return Pair("", "")

    val monthDayMatch = Regex("([A-Za-z]+) (\\d{1,2})").find(dateTimeLine)
        ?: return Pair("", "")


    val month = monthDayMatch.groupValues[1]
    val day = monthDayMatch.groupValues[2]

    val year = Regex("\\d{4}").find(dateTimeLine)?.value
        ?: LocalDate.now().year.toString()

    val localDate = try {
        LocalDate.parse(
            "$month $day $year",
            DateTimeFormatter.ofPattern("MMMM d yyyy", Locale.ENGLISH)
        )
    } catch (e: Exception) {
        return Pair("", "")
    }

    val timeMatch = Regex(
        "(\\d{1,2})(?::(\\d{2}))?([ap]m)?\\s*[-–]\\s*(\\d{1,2})(?::(\\d{2}))?([ap]m)"
    ).find(dateTimeLine)

    if(timeMatch != null) {
        val startHour = timeMatch.groupValues[1]
        val startMin = timeMatch.groupValues[2].ifBlank { "00" }
        val startPeriod = timeMatch.groupValues[3].ifBlank { timeMatch.groupValues[6] }
        val endHour = timeMatch.groupValues[4]
        val endMin = timeMatch.groupValues[5].ifBlank { "00" }
        val endPeriod = timeMatch.groupValues[6]

        val startTime = parseTimeManual("$startHour:$startMin$startPeriod")
        val endTime = parseTimeManual("$endHour:$endMin$endPeriod")

        return "${localDate}T$startTime" to "${localDate}T$endTime"

    }

    return "${localDate}T00:00" to "${localDate}T00:00"
}

fun parseTimeManual(timeStr: String?): String {
    if (timeStr.isNullOrBlank()) return "00:00"
    val cleaned = timeStr.trim().lowercase().replace("\\s+".toRegex(), "")

    // Match hour and am/pm
    val match = Regex("(\\d{1,2})(?::(\\d{2}))?(am|pm)")
        .find(cleaned) ?: return "00:00"
    val hour = match.groupValues[1].toInt()
    val minutes = match.groupValues[2].ifBlank { "00" }
    val period = match.groupValues[3]

    // Convert to 24-hour
    val hour24 = when (period) {
        "am" -> if (hour == 12) 0 else hour
        "pm" -> if (hour == 12) 12 else hour + 12
        else -> hour
    }

    return String.format("%02d:%s", hour24, minutes)
}


fun extractLocation(description: String): String? {
    val clean = description.cleanHtml()
    val firstLine = clean.lines().firstOrNull() ?: return null
    return firstLine.split("-").firstOrNull()?.trim()?.takeIf { it.isNotEmpty() }
}
