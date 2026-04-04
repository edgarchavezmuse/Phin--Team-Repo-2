package com.example.phinui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.phinui.ui.navigation.Routes
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import com.example.phinui.components.messages.UserListRepository
import com.example.phinui.components.messages.User


@Composable fun UserListScreen (navController: NavController) {
    val currentUserID = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val userListRepository = remember { UserListRepository() }

    var users by remember { mutableStateOf(listOf<User>()) }

    LaunchedEffect(Unit) {
        users = userListRepository.getAllUsers(currentUserID)
    }

    LazyColumn {
        items(users) { user ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable {
                        navController.navigate(Routes.MESSAGES + "/${user.uid}")
                    }
            ) {
                Text(text = user.name)
            }
        }
    }
}