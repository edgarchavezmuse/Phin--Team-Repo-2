package com.example.phinui.ui.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.phinui.data.calendar.CalendarEvent
import com.example.phinui.data.calendar.CalendarStorage
import com.example.phinui.data.events.EventData
import com.example.phinui.screens.CalendarScreen
import com.example.phinui.ui.screens.AddEventScreen
import com.example.phinui.ui.screens.EventsScreen
import com.example.phinui.ui.screens.MessagesScreen
import com.example.phinui.ui.screens.HomeScreen
import com.example.phinui.ui.screens.ProfileScreen
import com.example.phinui.viewmodel.CalendarViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.phinui.screens.MapScreen
import com.example.phinui.ui.screens.ScheduleScreen

@Composable
fun PhinNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // variables for ensuring events get passed to calendar
    val context = LocalContext.current
    val storeEvent = remember { CalendarStorage(context) }
    val savedEvents = remember { mutableStateListOf<CalendarEvent>() }
    val allEvents = remember { mutableStateListOf<CalendarEvent>() }
    val coroutineScope = rememberCoroutineScope()
    val calendarViewModel: CalendarViewModel = viewModel()

    //
    LaunchedEffect(Unit) {
        val loaded = storeEvent.loadEvents()

        savedEvents.clear()
        savedEvents.addAll(loaded)

        allEvents.clear()
        allEvents.addAll(EventData.eventList)

        loaded.forEach { savedEvent ->
            if (allEvents.none { it.id == savedEvent.id }) {
                allEvents.add(savedEvent)
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier
    ) {
        // navigate to x screen
        composable(Routes.HOME) {
            HomeScreen(navController = navController)
        }

        composable(Routes.MESSAGES) {
            MessagesScreen()
        }

        composable(Routes.PROFILE) {
            ProfileScreen()
        }

        composable(Routes.SCHEDULE) {
            ScheduleScreen()
        }

        composable(Routes.EVENTS) {
            EventsScreen(
                events = allEvents,
                onEventClick = { event ->
                    if (savedEvents.none { it.id == event.id }) {
                        savedEvents.add(event)
                        // Save to persistent storage
                        coroutineScope.launch(Dispatchers.IO) {
                            storeEvent.saveEvent(event)
                        }

                        // notification for adding event
                        Toast.makeText(
                            context,
                            "${event.title} added to your calendar.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // notification for event existing in calendar
                        Toast.makeText(
                            context,
                            "${event.title} already in your calendar.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                onAddEventClick = {
                    navController.navigate(Routes.ADD_EVENT) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.CALENDAR) {
            // to pass to onClick for removing events
            val selectedEvent = remember { mutableStateOf<CalendarEvent?>(null) }
            val showRemoveDialog = remember { mutableStateOf(false) }
            CalendarScreen(
                savedEvents = savedEvents,
                calendarViewModel = calendarViewModel,
                onClick = { event ->
                    selectedEvent.value = event
                    showRemoveDialog.value = true
                },
                selectedEvent = selectedEvent,
                showRemoveDialog = showRemoveDialog
            )
        }

        composable(Routes.ADD_EVENT) {
            AddEventScreen(
                onSaveEvent = { newEvent ->
                    if (allEvents.none { it.id == newEvent.id }) {
                        allEvents.add(newEvent)
                    }

                    if (savedEvents.none { it.id == newEvent.id }) {
                        savedEvents.add(newEvent)
                    }

                    coroutineScope.launch(Dispatchers.IO) {
                        storeEvent.saveEvent(newEvent)
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        composable(Routes.MAP) {
            MapScreen(navController = navController)
        }
    }
}