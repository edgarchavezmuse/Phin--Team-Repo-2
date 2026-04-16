package com.example.phinui.data.schedule

data class ScheduleClass(
    val id: String = "",
    val courseCode: String = "",
    val courseName: String = "",
    val days: List<String> = emptyList(),
    val startTime: String = "",
    val endTime: String = "",
    val location: String = ""
)