package com.example.phinui.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.phinui.ui.navigation.Routes
import com.example.phinui.ui.theme.Background
import com.example.phinui.ui.theme.HeaderRed

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun CustomBottomBar(
    navController: NavHostController,
    waveAnimation: WaveAnimationState,
    showWaves: Boolean = false // turns waves and ship on and off
) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    val bottomItems = listOf(
        BottomNavItem("Home", Icons.Default.Home, Routes.HOME),
        BottomNavItem("Map", Icons.Default.LocationOn, Routes.MAP),
        BottomNavItem("Calendar", Icons.Default.DateRange, Routes.CALENDAR),
        BottomNavItem("Messages", Icons.AutoMirrored.Filled.Message, Routes.USERLIST)
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = Background,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {

            // waves
            if (showWaves) {
                AnimatedWaves(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    waveColor = HeaderRed,
                    height = 90.dp,
                    showShip = true,
                    shipSize = 18.dp,
                    shipVerticalOffset = 15f,
                    frontPhase = waveAnimation.frontPhase,
                    backPhase = waveAnimation.backPhase,
                    shipProgress = waveAnimation.shipProgress,
                    bobPhase = waveAnimation.bobPhase
                )
            }

            // icons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                bottomItems.forEach { item ->
                    BottomBarItem(
                        label = item.label,
                        icon = item.icon,
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BottomBarItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val activeBackground = Color(0xFFFFE3E3)
    val activeColor = Color(0xFFD62828)
    val inactiveColor = Color(0xFF2C2C2C)

    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (selected) activeBackground else Color.Transparent,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) activeColor else inactiveColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) activeColor else inactiveColor,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}