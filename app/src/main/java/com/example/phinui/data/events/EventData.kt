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
            start = "2026-03-23T09:00",
            end = "2026-03-23T10:00",
            location = "del Norte Hall 1500",
            reminderMinutes = emptyList(),
            source = CalendarSource.LOCAL,
            description = "Need help with your academic path? " +
            "Meet with advisors to plan and discuss your Health Science careers!"
        ),
        CalendarEvent(
            id = "2",
            title = "Student Success Workshop",
            start = "2026-03-24T09:00",
            end = "2026-03-24T10:00",
            location = "Bell Tower Courtyard",
            reminderMinutes = emptyList(),
            source = CalendarSource.LOCAL,
            description = "Join us for a Student Success Workshop where you'll learn " +
            "effective study skills to help you prepare for tests and build confidence."
        ),
        CalendarEvent(
            id = "3",
            title = "Hackathon",
            start = "2026-03-25T13:00",
            end = "2026-03-25T14:00",
            location = "Sierra Hall 203",
            reminderMinutes = emptyList(),
            source = CalendarSource.LOCAL,
            description = "Have you ever wanted to see what happens in a hackathon?" +
            "Now's your chance to find out! Come down to Sierra Hall and start " +
            "your adventure in computer programming by participating in this year's hackathon."
        ),
        CalendarEvent(
            id = "4",
            title = "Picnic",
            start = "2026-03-26T15:00",
            end = "2026-03-26T16:00",
            location = "North Quad",
            reminderMinutes = emptyList(),
            source = CalendarSource.LOCAL,
            description = "College life can be stressful and nothing's better than " +
            "sitting outside with your friends soaking up the sun. Join us at the North Quad " +
            "for food, drinks, and activities!"
        ),
        CalendarEvent(
            id = "5",
            title = "Free Lunch",
            start = "2026-03-27T11:00",
            end = "2026-03-27T12:00",
            location = "Islands Cafe",
            reminderMinutes = emptyList(),
            source = CalendarSource.LOCAL,
            description = "Take a break from the studying with some free food!" +
            "The Islands Cafe will be serving free pizza, fries, salads, and more!"
        ),
        CalendarEvent(
            id = "6",
            title = "Free Lunch",
            start = "2026-04-01T11:00",
            end = "2026-04-01T12:00",
            location = "Islands Cafe",
            reminderMinutes = emptyList(),
            source = CalendarSource.LOCAL,
            description = "The Islands Cafe will be serving free iced coffee and sweet treats!"
        )
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