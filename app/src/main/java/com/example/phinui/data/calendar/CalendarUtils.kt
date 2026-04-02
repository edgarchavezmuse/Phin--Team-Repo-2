package com.example.phinui.data.calendar

// Checks if two events are the same (based on title + start time)
fun sameCalendarEvent(a: CalendarEvent, b: CalendarEvent): Boolean {
    return a.title.trim().equals(b.title.trim(), ignoreCase = true) &&
            a.start.take(16) == b.start.take(16)
}