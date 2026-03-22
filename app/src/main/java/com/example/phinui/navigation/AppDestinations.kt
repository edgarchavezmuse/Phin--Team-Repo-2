package com.example.phinui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    FAVORITES("Messages", Icons.AutoMirrored.Filled.Message),
    PROFILE("Profile", Icons.Default.AccountBox),
}