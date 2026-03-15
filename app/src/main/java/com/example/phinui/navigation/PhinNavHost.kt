package com.example.phinui.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.phinui.data.calendar.CalendarEvent
import com.example.phinui.screens.CalendarScreen
import com.example.phinui.ui.screens.EventsScreen
import com.example.phinui.ui.screens.FavoritesScreen
import com.example.phinui.ui.screens.HomeScreen
import com.example.phinui.ui.screens.ProfileScreen

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
            EventsScreen(onEventClick = { event ->
                // test dialog box functionality
                if (savedEvents.none { it.id == event.id}) {
                    savedEvents.add(event)
                }
                // Show 'add to calendar' message
            })
        }

        composable(Routes.CALENDAR) {
            CalendarScreen()
        }
    }
}