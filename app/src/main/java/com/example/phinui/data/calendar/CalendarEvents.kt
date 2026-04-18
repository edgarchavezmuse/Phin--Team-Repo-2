package com.example.phinui.data.calendar

enum class CalendarSource {
    LOCAL,
    GOOGLE
}

data class CalendarEvent(
    val id: String,
    val title: String,
    val start: String,
    val end: String,
    // to ensure EventData.kt can hold event location info
    val location: String? = null,
    // for reminders set in google calendar
    val reminderMinutes: List<Int> = emptyList(),
    val source: CalendarSource,
    val description: String? = null,
    val isAllDay: Boolean = false,
    val colorHex: String = "#FF1F1F"
)