package com.example.phinui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.phinui.ui.components.UserAvatar
import com.example.phinui.ui.navigation.Routes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
        MenuItem("People", Icons.Default.PersonAdd, Routes.PEOPLE),
        MenuItem("Friends", Icons.Default.People, Routes.FRIENDS),
        MenuItem("Messages", Icons.AutoMirrored.Filled.Message, Routes.USERLIST),
        MenuItem("Events", Icons.Default.Event, Routes.EVENTS),
        MenuItem("Calendar", Icons.Default.DateRange, Routes.CALENDAR),
        MenuItem("Map", Icons.Default.LocationOn, Routes.MAP)
    )

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val user = auth.currentUser

    var name by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }

    user?.uid?.let { uid ->
        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                name = document.getString("name") ?: "No Name"
                if (name != "No Name") {
                    firstName = name.substringBefore(" ")
                }
            }
    }

    Column(modifier = Modifier.padding(16.dp)) {

        Row() {

            UserAvatar(name = name, size = 35)

            Spacer(modifier = Modifier.width(16.dp))

            Box(
                modifier = Modifier.padding(top = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Welcome" +
                            if (name == "No Name") "!" else ", $firstName!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }

        }

        Spacer(modifier = Modifier.height(12.dp))

        menuItems.forEach { item ->
            MenuRowBuilder(
                label = item.label,
                icon = item.icon,
                isSelected = currentRoute == item.route,
                onItemClick,
                onClick = {
                    navController.navigate(item.route) {
                        launchSingleTop = true
                        restoreState = true
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
            .clickable {
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