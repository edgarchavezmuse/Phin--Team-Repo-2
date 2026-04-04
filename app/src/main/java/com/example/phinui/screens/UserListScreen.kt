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
//import com.google.firebase.firestore.auth.User
import androidx.compose.material3.Text

data class User(
    val uid: String,
    val name: String
)

@Composable fun UserListScreen (navController: NavController, users: List<User>) {
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