package com.example.phinui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.phinui.ui.navigation.Routes

data class MenuItem (
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun SideMenu(
    navController: NavHostController,
    onItemClick: () -> Unit
) {
    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route

    val menuItems = listOf(
        MenuItem("Home", Icons.Default.Home, Routes.HOME),
        MenuItem("Profile", Icons.Default.AccountBox, Routes.PROFILE),
        MenuItem("Messages", Icons.AutoMirrored.Filled.Message, Routes.MESSAGES),
        MenuItem("Events", Icons.Default.Event, Routes.EVENTS),
        MenuItem("Calendar", Icons.Default.DateRange, Routes.CALENDAR),
        MenuItem("Map", Icons.Default.LocationOn, Routes.MAP)
    )

    Column(modifier = Modifier.padding(16.dp)) {
        menuItems.forEach { item ->
            MenuRowBuilder(
                label = item.label,
                icon = item.icon,
                isSelected = currentRoute == item.route,
                onItemClick,
                onClick = {
                    if (item.route == Routes.HOME) {
                        navController.popBackStack(Routes.HOME, false)
                    } else {
                        navController.navigate(item.route) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    onItemClick()
                }
            )
        }
    }
}

@Composable
fun MenuRowBuilder(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onItemClick: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) Color.Gray.copy(alpha = 0.2f) else Color.Transparent
            )
            .clickable{
                onClick()
                onItemClick()
            }
            .padding(vertical = 12.dp)
    ) {
        Icon(icon, label)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label)
    }
}