package com.example.phinui.data.events

import com.example.phinui.data.calendar.CalendarEvent
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/* Moved event list from EventsScreen.kt to this file instead.
Converted the list from EventCard to be a parameter we can pass
 */
object EventData {
    // For displaying Date as Month Day (i.e. Jan 1)
    private val displayDateFormatter = DateTimeFormatter.ofPattern("MMM d")
    val eventList = listOf(
        CalendarEvent("1", "Health Science Advising Sessions", "2026-03-09T09:00", "2026-03-09T10:00", "del Norte Hall 1500"),
        CalendarEvent("2", "Student Success Workshop", "2026-03-10T09:00", "2026-03-10T10:00", "Bell Tower Courtyard"),
        CalendarEvent("3", "Events 3 Name", "2026-03-11T13:00", "2026-03-11T14:00", "TBD"),
        CalendarEvent("4", "Events 4 Name", "2026-03-12T15:00", "2026-03-12T16:00", "TBD"),
        CalendarEvent("5", "Events 5 Name", "2026-03-13T11:00", "2026-03-13T12:00", "TBD")
    )

    // For displaying time as HH:MM (i.e. 12:30)
    fun formatEventDateTime(event: CalendarEvent): String{
        return try {
            val datePart = LocalDate.parse(event.start.substring(0,10))
                .format(displayDateFormatter)
            val timePart = if(event.start.contains('T') && event.start.length >= 16) {
                event.start.substring(11,16)
            }
            else ""
            "Date: $datePart\nTime: $timePart"
        } catch (_: Exception) {
            "Date: TBD\nTime: TBD"
        }
    }
}