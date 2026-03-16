package com.example.phinui.ui.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.phinui.data.calendar.CalendarEvent
import com.example.phinui.data.calendar.CalendarStorage
import com.example.phinui.data.events.EventData
import com.example.phinui.screens.CalendarScreen
import com.example.phinui.ui.screens.EventsScreen
import com.example.phinui.ui.screens.FavoritesScreen
import com.example.phinui.ui.screens.HomeScreen
import com.example.phinui.ui.screens.ProfileScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.phinui.viewmodel.CalendarViewModel

@Composable
fun PhinNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // variables for ensuring events get passed to calendar
    val context = LocalContext.current
    val storeEvent = remember { CalendarStorage(context) }
    val savedEvents = remember { mutableStateListOf<CalendarEvent>() }
    val coroutineScope = rememberCoroutineScope()
    val calendarViewModel: CalendarViewModel = viewModel()

    //
    LaunchedEffect(Unit) {
        val loaded = storeEvent.loadEvents()
        savedEvents.addAll(loaded)
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

        composable(Routes.FAVORITES) {
            FavoritesScreen()
        }

        composable(Routes.PROFILE) {
            ProfileScreen()
        }

        composable(Routes.EVENTS) {
            val allEvents = remember {
                mutableStateListOf<CalendarEvent>().apply {
                    addAll(EventData.eventList)
                }
            }

            EventsScreen(
                events = allEvents,
                onEventClick = { event ->
                    if (savedEvents.none { it.id == event.id }) {
                        savedEvents.add(event)
                        // Save to persistent storage
                        coroutineScope.launch(Dispatchers.IO){
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
                }
            )
        }

        composable(Routes.CALENDAR) {
            CalendarScreen(
                savedEvents = savedEvents,
                calendarViewModel = calendarViewModel
            )
        }
    }
}