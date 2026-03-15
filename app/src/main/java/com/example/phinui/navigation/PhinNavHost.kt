package com.example.phinui.ui.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.phinui.data.calendar.CalendarEvent
import com.example.phinui.data.calendar.CalendarStorage
import com.example.phinui.screens.CalendarScreen
import com.example.phinui.ui.screens.EventsScreen
import com.example.phinui.ui.screens.FavoritesScreen
import com.example.phinui.ui.screens.HomeScreen
import com.example.phinui.ui.screens.ProfileScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PhinNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
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
            // to hold the event selected to add to the calendar
            val savedEvents = remember {mutableStateListOf<CalendarEvent>()}

            // for storing the event in calendar - persistence
            val currentContext = LocalContext.current

            // for event to stay persistent in calendar
            val storeEvent = CalendarStorage(currentContext)

            // display saved events in calendar
            LaunchedEffect(Unit) {
                val loadedEvents = storeEvent.loadEvents()
                savedEvents.addAll(loadedEvents)
            }

            EventsScreen(onEventClick = { event ->
                // Show 'add to calendar?' message
                CoroutineScope(Dispatchers.IO).launch {

                    // check if clicked event already exists in calendar
                    if (savedEvents.any { it.id == event.id }) {
                        withContext(Dispatchers.Main){
                            Toast.makeText(
                                currentContext,
                                "${event.title} already in your calendar.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        // stores event in calendar and is persistent
                        storeEvent.saveEvent(event)

                        // updates list of events in calendar
                        savedEvents.add(event)

                        // confirmation message
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                currentContext,
                                "${event.title} added to your calendar.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } )
        }

        composable(Routes.CALENDAR) {
            CalendarScreen()
        }
    }
}