package com.example.phinui.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.phinui.data.calendar.CalendarEvent
import com.example.phinui.ui.components.TopHeader
import com.example.phinui.ui.components.widgets.AddScheduleSheet
import com.example.phinui.ui.navigation.Routes
import com.example.phinui.ui.theme.Background
import com.example.phinui.ui.viewmodel.ScheduleViewModel

@Composable
fun HomeScreen(
    navController: NavHostController,
    events: List<CalendarEvent>,
    isLoading: Boolean
) {
    var showAddSheet by remember { mutableStateOf(false) }
    val scheduleViewModel: ScheduleViewModel = viewModel()
    val classes by scheduleViewModel.classes.collectAsState()
    val catalogCourses by scheduleViewModel.catalogCourses.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

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
                classes = classes,
                onOpenEvents = {
                    navController.navigate(Routes.EVENTS) { launchSingleTop = true }
                },
                onOpenCalendar = {
                    navController.navigate(Routes.CALENDAR) { launchSingleTop = true }
                },
                onOpenMap = {
                    navController.navigate(Routes.MAP)
                },
                onAddClass = {
                    showAddSheet = true
                },
                onViewSchedule = {
                    navController.navigate(Routes.SCHEDULE) { launchSingleTop = true }
                },
                onOpenMessages = {
                    navController.navigate(Routes.USERLIST)
                },
                onDeleteClass = { scheduleClass ->
                    scheduleViewModel.deleteClass(scheduleClass) {
                        Toast.makeText(
                            context,
                            "${scheduleClass.courseCode} removed from schedule",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }

        if (showAddSheet) {
            AddScheduleSheet(
                catalogCourses = catalogCourses,
                onDismiss = { showAddSheet = false },
                onSave = { scheduleClass ->
                    scheduleViewModel.addClass(scheduleClass) {
                        showAddSheet = false

                        Toast.makeText(
                            context,
                            "${scheduleClass.courseCode} added to schedule",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }
    }
}