package com.example.phinui.data.calendar

// Checks if two events are the same (based on title + start time)
fun sameCalendarEvent(a: CalendarEvent, b: CalendarEvent): Boolean {
    return a.title.trim().equals(b.title.trim(), ignoreCase = true) &&
            a.start.take(16) == b.start.take(16)
}

// suggested code
//fun sameCalendarEvent(a: CalendarEvent, b: CalendarEvent): Boolean {
    // If both have Google IDs, compare them
//    if (!a.googleEventID.isNullOrBlank() && !b.googleEventID.isNullOrBlank()) {
//        return a.googleEventID == b.googleEventID
//    }

    // Fallback: compare title + start time (your original logic)
//    return a.title.trim().equals(b.title.trim(), ignoreCase = true) &&
//            a.start.take(16) == b.start.take(16)
//}