package com.example.phinui.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.phinui.data.calendar.CalendarEvent
import com.example.phinui.ui.components.TopHeader
import com.example.phinui.ui.navigation.Routes
import com.example.phinui.ui.theme.Background

@Composable
fun HomeScreen(
    navController: NavHostController,
    events: List<CalendarEvent>,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        TopHeader()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            HomeDashboard(
                events = events,
                isLoading = isLoading,
                onOpenEvents = {
                    navController.navigate(Routes.EVENTS) { launchSingleTop = true }
                },
                onOpenCalendar = {
                    navController.navigate(Routes.CALENDAR) { launchSingleTop = true }
                },
                onOpenMap = {
                    navController.navigate(Routes.MAP)
                },
                onOpenSchedule = {
                    navController.navigate(Routes.SCHEDULE) { launchSingleTop = true }
                }
            )
        }
    }
}