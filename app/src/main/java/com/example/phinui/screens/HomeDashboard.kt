package com.example.phinui.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.phinui.data.calendar.CalendarEvent
import com.example.phinui.data.schedule.ScheduleClass
import com.example.phinui.ui.components.widgets.CampusEventsWidget
import com.example.phinui.ui.components.widgets.CourseScheduleWidget

@Composable
fun HomeDashboard(
    // makes the xx icons clickable
    events: List<CalendarEvent>,
    onOpenEvents: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenMap: () -> Unit,
    onAddClass: () -> Unit,
    onViewSchedule: () -> Unit,
    onOpenMessages: () -> Unit,
    classes: List<ScheduleClass>,
    isLoading: Boolean,
    onDeleteClass: (ScheduleClass) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CampusEventsWidget(
            events = events,
            isLoading = isLoading,
            onViewAllClick = onOpenEvents
        )
        CourseScheduleWidget(
            classes = classes,
            onAddClass = onAddClass,
            onViewSchedule = onViewSchedule,
            onDeleteClass = onDeleteClass
        )
    }
}