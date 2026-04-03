package com.example.phinui.data.events

import com.example.phinui.data.calendar.CalendarEvent
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.phinui.data.calendar.CalendarSource

/* Moved event list from EventsScreen.kt to this file instead.
Converted the list from EventCard to be a parameter we can pass
 */
object EventData {
    // For displaying Date as Month Day (i.e. Jan 1)
    private val displayDateFormatter = DateTimeFormatter.ofPattern("MMM d")
    val eventList = listOf(
        CalendarEvent(
            id = "1",
            title = "Health Science Advising Sessions",
            start = "2026-04-04T09:00",
            end = "2026-04-07T10:00",
            location = "del Norte Hall 1500",
            reminderMinutes = emptyList(),
            source = CalendarSource.LOCAL
        ),
        CalendarEvent(
            id = "2",
            title = "Student Success Workshop",
            start = "2026-04-04T09:00",
            end = "2026-04-05T10:00",
            location = "Bell Tower Courtyard",
            reminderMinutes = emptyList(),
            source = CalendarSource.LOCAL
        ),
        CalendarEvent(
            id = "3",
            title = "Hackathon",
            start = "2026-04-04T13:00",
            end = "2026-04-04T14:00",
            location = "Sierra Hall 203",
            reminderMinutes = emptyList(),
            source = CalendarSource.LOCAL
        ),
        CalendarEvent(
            id = "4",
            title = "Picnic",
            start = "2026-04-04T15:00",
            end = "2026-04-04T16:00",
            location = "North Quad",
            reminderMinutes = emptyList(),
            source = CalendarSource.LOCAL
        ),
        CalendarEvent(
            id = "5",
            title = "Free Lunch",
            start = "2026-03-27T11:00",
            end = "2026-03-27T12:00",
            location = "Islands Cafe",
            reminderMinutes = emptyList(),
            source = CalendarSource.LOCAL
        )
    )
}