package com.example.phinui.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.phinui.screens.CalendarScreen
import com.example.phinui.screens.MapScreen
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
            EventsScreen()
        }

        composable(Routes.CALENDAR) {
            CalendarScreen()
        }
        composable(Routes.MAP) {
            MapScreen(navController = navController)
        }
    }
}