package com.example.phinui.data.calendar

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.ZoneId

/*
 * Handles communication with Google Calendar API.
 * Deals with calendar event operations.
 */
object GoogleCalendarRepository {

    // Fetches events for the week you choose to look at in the app.
    suspend fun fetchWeekEvents(
        accessToken: String,
        weekStart: LocalDate,
        zone: ZoneId = ZoneId.systemDefault()
    ): List<CalendarEvent> = withContext(Dispatchers.IO) {

        val events = mutableListOf<CalendarEvent>()

        // A page token = a pointer to the next “page” of results.
        var pageToken: String? = null

        val weekStartTimestamp = weekStart.atStartOfDay(zone).toInstant().toString()
        val weekEndTimestamp = weekStart.plusDays(7).atStartOfDay(zone).toInstant().toString()

        do {
            val url = buildEventsRequestUrl(
                timeMin = weekStartTimestamp,
                timeMax = weekEndTimestamp,
                pageToken = pageToken
            )

            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Authorization", "Bearer $accessToken")
            connection.setRequestProperty("Accept", "application/json")

            // Read response and error handle
            try {
                val code = connection.responseCode

                // Read from inputStream when successful, otherwise from errorStream
                val body = if (code in 200..299) {
                    connection.inputStream.bufferedReader().readText()
                } else {
                    connection.errorStream?.bufferedReader()?.readText() ?: ""
                }

                if (code == 401) {
                    throw GoogleCalendarUnauthorizedException()
                }

                // Fail fast if the API returned an error status code
                if (code !in 200..299) {
                    throw RuntimeException("Calendar API error ($code): $body")
                }

                // Parse JSON and map results into CalendarEvent objects
                val root = JSONObject(body)

                // "items" is the array of events returned by the Calendar API
                val items = root.optJSONArray("items")
                if (items != null) {
                    for (i in 0 until items.length()) {
                        val item = items.getJSONObject(i)
                        events.add(parseGoogleEvent(item))
                    }
                }

                pageToken = root.optString("nextPageToken", null)
            } finally {
                connection.disconnect()
            }
        } while (pageToken != null)

        events
    }

    // Inserts a new event into the user's primary Google Calendar
    suspend fun insertEvent(
        accessToken: String,
        event: CalendarEvent,
        zone: ZoneId = ZoneId.systemDefault()
    ): CalendarEvent = withContext(Dispatchers.IO) {

        val url = URL("https://www.googleapis.com/calendar/v3/calendars/primary/events")
        val connection = url.openConnection() as HttpURLConnection

        connection.requestMethod = "POST"
        connection.setRequestProperty("Authorization", "Bearer $accessToken")
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        connection.doOutput = true

        try {
            val requestBody = buildInsertEventBody(event, zone).toString()

            connection.outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.write(requestBody)
                writer.flush()
            }

            val code = connection.responseCode
            val body = if (code in 200..299) {
                connection.inputStream.bufferedReader().readText()
            } else {
                connection.errorStream?.bufferedReader()?.readText() ?: ""
            }

            if (code == 401) {
                throw GoogleCalendarUnauthorizedException()
            }

            if (code !in 200..299) {
                throw RuntimeException("Calendar insert error ($code): $body")
            }

            val root = JSONObject(body)
            parseGoogleEvent(root)
        } finally {
            connection.disconnect()
        }
    }

    // Builds the full google calendar api url string
    private fun buildEventsRequestUrl(
        timeMin: String,
        timeMax: String,
        pageToken: String?
    ): String {

        val builder = Uri.parse(
            "https://www.googleapis.com/calendar/v3/calendars/primary/events"
        ).buildUpon()

        builder.appendQueryParameter("singleEvents", "true")
        builder.appendQueryParameter("orderBy", "startTime")
        builder.appendQueryParameter("maxResults", "2500")
        builder.appendQueryParameter("timeMin", timeMin)
        builder.appendQueryParameter("timeMax", timeMax)

        if (pageToken != null) {
            builder.appendQueryParameter("pageToken", pageToken)
        }

        return builder.build().toString()
    }

    // Builds the JSON body for inserting an event into Google Calendar
    private fun buildInsertEventBody(
        event: CalendarEvent,
        zone: ZoneId
    ): JSONObject {
        val timeZoneId = zone.id

        return JSONObject().apply {
            put("summary", event.title)

            put("colorId", mapHexToGoogleColorId(event.colorHex))

            if (!event.description.isNullOrBlank()) {
                put("description", event.description)
            }

            if (!event.location.isNullOrBlank()) {
                put("location", event.location)
            }

            if (event.isAllDay) {
                put(
                    "start",
                    JSONObject().apply {
                        put("date", event.start)
                    }
                )

                put(
                    "end",
                    JSONObject().apply {
                        put("date", getNextDate(event.end))
                    }
                )
            } else {
                put(
                    "start",
                    JSONObject().apply {
                        put("dateTime", normalizeDateTime(event.start))
                        put("timeZone", timeZoneId)
                    }
                )

                put(
                    "end",
                    JSONObject().apply {
                        put("dateTime", normalizeDateTime(event.end))
                        put("timeZone", timeZoneId)
                    }
                )
            }

            put(
                "reminders",
                JSONObject().apply {
                    put("useDefault", false)

                    val overrides = JSONArray()
                    event.reminderMinutes.distinct().forEach { minutes ->
                        overrides.put(
                            JSONObject().apply {
                                put("method", "popup")
                                put("minutes", minutes)
                            }
                        )
                    }

                    put("overrides", overrides)
                }
            )
        }
    }

    // Parses a Google Calendar API event object into CalendarEvent
    private fun parseGoogleEvent(item: JSONObject): CalendarEvent {
        val id = item.optString("id")
        val title = item.optString("summary", "(No title)")

        val startObj = item.optJSONObject("start")
        val endObj = item.optJSONObject("end")

        val startDateTime = startObj?.optString("dateTime")?.ifBlank { null }
        val endDateTime = endObj?.optString("dateTime")?.ifBlank { null }
        val startDate = startObj?.optString("date")?.ifBlank { null }
        val endDate = endObj?.optString("date")?.ifBlank { null }

        val isAllDay = startDate != null

        val start = startDateTime ?: startDate ?: ""
        val end = if (isAllDay) {
            // Google returns all-day end dates as exclusive, so shift back by 1 day
            endDate?.let { getPreviousDate(it) } ?: ""
        } else {
            endDateTime ?: endDate ?: ""
        }

        val remindersList = mutableListOf<Int>()
        val remindersObj = item.optJSONObject("reminders")
        if (remindersObj != null) {
            val overrides = remindersObj.optJSONArray("overrides")
            if (overrides != null) {
                for (j in 0 until overrides.length()) {
                    val override = overrides.getJSONObject(j)
                    val minutes = override.optInt("minutes", -1)
                    if (minutes >= 0) {
                        remindersList.add(minutes)
                    }
                }
            }
        }

        val location = item.optString("location").ifBlank { null }
        val description = item.optString("description").ifBlank { null }
        val colorId = item.optString("colorId").ifBlank { null }

        return CalendarEvent(
            id = id,
            title = title,
            start = start,
            end = end,
            location = location,
            reminderMinutes = remindersList,
            source = CalendarSource.GOOGLE,
            description = description,
            isAllDay = isAllDay,
            colorHex = mapGoogleColorIdToHex(colorId)
        )
    }

    // Ensures datetime strings are in a valid format for Google Calendar API
    private fun normalizeDateTime(dateTime: String): String {
        return when {
            dateTime.length == 16 && dateTime.contains('T') -> "$dateTime:00"
            else -> dateTime
        }
    }
    private fun getNextDate(date: String): String {
        return LocalDate.parse(date).plusDays(1).toString()
    }

    private fun getPreviousDate(date: String): String {
        return LocalDate.parse(date).minusDays(1).toString()
    }

    // Deletes events
    suspend fun deleteEvent(
        accessToken: String,
        eventId: String
    ) = withContext(Dispatchers.IO) {

        val encodedEventId = Uri.encode(eventId)
        val url = URL("https://www.googleapis.com/calendar/v3/calendars/primary/events/$encodedEventId")
        val connection = url.openConnection() as HttpURLConnection

        connection.requestMethod = "DELETE"
        connection.setRequestProperty("Authorization", "Bearer $accessToken")
        connection.setRequestProperty("Accept", "application/json")

        try {
            val code = connection.responseCode

            if (code !in 200..299 && code != 204 && code != 410) {
                val body = connection.errorStream?.bufferedReader()?.readText() ?: ""
                throw RuntimeException("Calendar delete error ($code): $body")
            }
        } finally {
            connection.disconnect()
        }
    }
}

private fun mapHexToGoogleColorId(hex: String?): String {
    return when (hex?.trim()?.uppercase()) {
        "#7986CB" -> "1"
        "#33B679" -> "2"
        "#8E24AA" -> "3"
        "#E67C73" -> "4"
        "#F6BF26" -> "5"
        "#F4511E" -> "6"
        "#039BE5" -> "7"
        "#616161" -> "8"
        "#3F51B5" -> "9"
        else -> "4"
    }
}

private fun mapGoogleColorIdToHex(colorId: String?): String {
    return when (colorId) {
        "1" -> "#7986CB"
        "2" -> "#33B679"
        "3" -> "#8E24AA"
        "4" -> "#E67C73"
        "5" -> "#F6BF26"
        "6" -> "#F4511E"
        "7" -> "#039BE5"
        "8" -> "#616161"
        "9" -> "#3F51B5"
        else -> "#FF1F1F"
    }
}