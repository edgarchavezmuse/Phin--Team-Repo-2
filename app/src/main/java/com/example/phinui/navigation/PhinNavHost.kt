package com.example.phinui.ui.navigation

import android.widget.Toast
import androidx.activity.ComponentActivity
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
import androidx.navigation.navDeepLink
import com.example.phinui.data.calendar.CalendarEvent
import com.example.phinui.data.calendar.CalendarStorage
import com.example.phinui.data.events.EventData
import com.example.phinui.screens.CalendarScreen
import com.example.phinui.ui.screens.AddEventScreen
import com.example.phinui.ui.screens.EventsScreen
import com.example.phinui.ui.screens.MessagesScreen
import com.example.phinui.ui.screens.HomeScreen
import com.example.phinui.ui.screens.ProfileScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.phinui.ui.screens.MapScreen
import com.example.phinui.notifications.ReminderScheduler
import com.example.phinui.viewmodel.CalendarViewModel
import com.example.phinui.viewmodel.CalendarViewModelFactory
import com.example.phinui.data.calendar.sameCalendarEvent
import kotlinx.coroutines.withContext
import com.example.phinui.viewmodel.AddEventResult
import com.example.phinui.ui.screens.ScheduleScreen
import com.example.phinui.data.calendar.CalendarSource

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
            val context = LocalContext.current
            val activity = context as ComponentActivity

            val reminderScheduler = remember {
                ReminderScheduler(context.applicationContext)
            }

            val calendarStorage = remember { CalendarStorage(context) }
            val factory = remember {
                CalendarViewModelFactory(reminderScheduler, calendarStorage)
            }

            val calendarViewModel: CalendarViewModel = viewModel(
                viewModelStoreOwner = activity,
                factory = factory
            )

            EventsScreen(
                events = allEvents,
                onEventClick = { event ->
                    val googleEvents = calendarViewModel.eventsGroupedByDate.values.flatten()
                    val isSignedInToGoogle = calendarViewModel.googleAccessToken != null

                    val existsLocally = savedEvents.any { sameCalendarEvent(it, event) }
                    val existsInGoogle = googleEvents.any { sameCalendarEvent(it, event) }

                    if (isSignedInToGoogle) {
                        if (existsInGoogle) {
                            Toast.makeText(
                                context,
                                "${event.title} already in your Google Calendar.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            coroutineScope.launch {
                                try {
                                    when (val result = calendarViewModel.addEventToAppropriateCalendar(event)) {
                                        is AddEventResult.ShouldSaveLocally -> {
                                            if (!existsLocally) {
                                                savedEvents.add(event.copy(source = CalendarSource.LOCAL))

                                                withContext(Dispatchers.IO) {
                                                    storeEvent.saveEvent(event)
                                                }
                                            }

                                            Toast.makeText(
                                                context,
                                                "${event.title} added to your local calendar.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                        is AddEventResult.AddedToGoogle -> {
                                            Toast.makeText(
                                                context,
                                                "${event.title} added to your Google Calendar.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        e.message ?: "Failed to add event.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    } else {
                        if (existsLocally) {
                            Toast.makeText(
                                context,
                                "${event.title} already in your local calendar.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            coroutineScope.launch {
                                try {
                                    when (val result = calendarViewModel.addEventToAppropriateCalendar(event)) {
                                        is AddEventResult.ShouldSaveLocally -> {
                                            savedEvents.add(event.copy(source = CalendarSource.LOCAL))

                                            withContext(Dispatchers.IO) {
                                                storeEvent.saveEvent(event)
                                            }

                                            Toast.makeText(
                                                context,
                                                "${event.title} added to your local calendar.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                        is AddEventResult.AddedToGoogle -> {
                                            Toast.makeText(
                                                context,
                                                "${event.title} added to your Google Calendar.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        e.message ?: "Failed to add event.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                },
                onAddEventClick = {
                    navController.navigate(Routes.ADD_EVENT) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            Routes.CALENDAR,
            deepLinks = listOf(
                navDeepLink {
                    // deep link allows notification to navigate to this screen
                    uriPattern = "phin://calendar"
                }
            )
        ) {

            // setup for ViewModelFactory
            val context = LocalContext.current
            val activity = context as ComponentActivity

            val reminderScheduler = remember {
                ReminderScheduler(context.applicationContext)
            }

            val calendarStorage = remember { CalendarStorage(context) }
            val factory = remember {
                CalendarViewModelFactory(reminderScheduler, calendarStorage)
            }

            val calendarViewModel: CalendarViewModel = viewModel(
                viewModelStoreOwner = activity, // object that owns the ViewModel
                factory = factory
            )

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
            val context = LocalContext.current
            val activity = context as ComponentActivity

            val reminderScheduler = remember {
                ReminderScheduler(context.applicationContext)
            }

            val calendarStorage = remember { CalendarStorage(context) }
            val factory = remember {
                CalendarViewModelFactory(reminderScheduler, calendarStorage)
            }

            val calendarViewModel: CalendarViewModel = viewModel(
                viewModelStoreOwner = activity,
                factory = factory
            )

            AddEventScreen(
                onSaveEvent = { newEvent ->
                    coroutineScope.launch {
                        try {
                            when (val result = calendarViewModel.addEventToAppropriateCalendar(newEvent)) {
                                is AddEventResult.ShouldSaveLocally -> {
                                    if (allEvents.none { it.title == newEvent.title && it.start == newEvent.start }) {
                                        allEvents.add(newEvent)
                                    }

                                    if (savedEvents.none { it.title == newEvent.title && it.start == newEvent.start }) {
                                        savedEvents.add(newEvent.copy(source = CalendarSource.LOCAL))
                                    }

                                    withContext(Dispatchers.IO) {
                                        storeEvent.saveEvent(newEvent)
                                    }

                                    Toast.makeText(
                                        context,
                                        "${newEvent.title} added to your local calendar.",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    navController.popBackStack()
                                }

                                is AddEventResult.AddedToGoogle -> {
                                    if (allEvents.none { it.title == newEvent.title && it.start == newEvent.start }) {
                                        allEvents.add(newEvent)
                                    }

                                    Toast.makeText(
                                        context,
                                        "${newEvent.title} added to your Google Calendar.",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    navController.popBackStack()
                                }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                e.message ?: "Failed to save event.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.MAP) {
            MapScreen()
        }
    }
}
