package com.example.phinui.ui.navigation

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

import kotlinx.coroutines.withContext
import com.example.phinui.viewmodel.AddEventResult

import com.example.phinui.ui.screens.ScheduleScreen
import com.example.phinui.data.calendar.CalendarSource

//firebase
import com.example.phinui.ui.screens.LoginScreen
import com.example.phinui.ui.screens.RegisterScreen
import com.example.phinui.viewmodel.EventsRepository
import com.example.phinui.viewmodel.EventsViewModel
import com.example.phinui.viewmodel.EventsViewModelFactory
import com.google.firebase.auth.FirebaseAuth


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
    val auth = remember { FirebaseAuth.getInstance() }
    val startDestination = if (auth.currentUser != null) Routes.HOME else Routes.LOGIN

    // is this still needed?
    /*
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

     */

    val activity = context as ComponentActivity

    val eventFactory = remember {
        EventsViewModelFactory(
            repository = EventsRepository()
        )
    }

    val eventsViewModel: EventsViewModel = viewModel(
        viewModelStoreOwner = activity,
        factory = eventFactory
    )

    val isLoading by eventsViewModel.isLoading.collectAsState()
    val schoolEvents by eventsViewModel.events.collectAsState()

    NavHost(
        navController = navController,
        //startDestination = Routes.HOME,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onOpenRegister = {
                    navController.navigate(Routes.REGISTER) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onOpenLogin = {
                    navController.popBackStack()
                }
            )
        }

        // navigate to x screen
        composable(Routes.HOME) {
            HomeScreen(
                navController = navController,
                events = schoolEvents,
                isLoading = isLoading
            )
        }

        composable(Routes.MESSAGES) {
            MessagesScreen()
        }

        composable(Routes.PROFILE) {
            ProfileScreen(navController = navController)
        }

        composable(Routes.SCHEDULE) {
            ScheduleScreen()
        }

        composable(Routes.EVENTS) {
            val context = LocalContext.current

            val reminderScheduler = remember {
                ReminderScheduler(context.applicationContext)
            }

            val factory = remember {
                CalendarViewModelFactory(
                    context = context.applicationContext,
                    reminderScheduler = reminderScheduler
                )
            }

            val calendarViewModel: CalendarViewModel = viewModel(
                viewModelStoreOwner = activity,
                factory = factory
            )

            EventsScreen(
                //events = allEvents,
                events = schoolEvents,
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

            val reminderScheduler = remember {
                ReminderScheduler(context.applicationContext)
            }

            val factory = remember {
                CalendarViewModelFactory(
                    context = context.applicationContext,
                    reminderScheduler = reminderScheduler
                )
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
            val reminderScheduler = remember {
                ReminderScheduler(context.applicationContext)
            }

            val factory = remember {
                CalendarViewModelFactory(
                    context = context.applicationContext,
                    reminderScheduler = reminderScheduler
                )
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

// Checks if two events are the same (based on title + start time)
private fun sameCalendarEvent(a: CalendarEvent, b: CalendarEvent): Boolean {
    return a.title.trim().equals(b.title.trim(), ignoreCase = true) &&
            a.start.take(16) == b.start.take(16)
}
