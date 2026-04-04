package com.example.phinui.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.phinui.data.calendar.CalendarEvent
import com.example.phinui.ui.components.widgets.CampusEventsWidget
@Composable
fun HomeDashboard(
    // makes the xx icons clickable
    events: List<CalendarEvent>,
    onOpenEvents: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenSchedule: () -> Unit,
    isLoading: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CampusEventsWidget(
            events = events,
            isLoading = isLoading,
            onViewAllClick = onOpenEvents
        )
    }
}