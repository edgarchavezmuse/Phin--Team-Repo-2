package com.example.phinui.data.calendar

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import android.net.Uri
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
        val weekEndTimestamp  = weekStart.plusDays(7).atStartOfDay(zone).toInstant().toString()

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

                        val id = item.optString("id")
                        val title = item.optString("summary", "(No title)")

                        val startObj = item.optJSONObject("start")
                        val endObj = item.optJSONObject("end")

                        val start = startObj?.optString("dateTime")?.ifBlank { null }
                            ?: startObj?.optString("date")?.ifBlank { null }
                            ?: ""

                        val end = endObj?.optString("dateTime")?.ifBlank { null }
                            ?: endObj?.optString("date")?.ifBlank { null }
                            ?: ""

                        // for reminders
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

                        events.add(
                            CalendarEvent(
                                id = id,
                                title = title,
                                start = start,
                                end = end,
                                location = location,
                                reminderMinutes = remindersList,
                                source = CalendarSource.GOOGLE
                            )
                        )
                    }
                }

                pageToken = root.optString("nextPageToken", null)
            } finally {
                connection.disconnect()
            }
        } while (pageToken != null)

        events
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
}