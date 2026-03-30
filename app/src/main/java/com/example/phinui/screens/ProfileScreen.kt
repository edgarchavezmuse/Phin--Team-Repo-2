package com.example.phinui.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.example.phinui.ui.theme.Background
import com.example.phinui.ui.theme.NavText
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.phinui.ui.navigation.Routes
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.*
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ProfileScreen(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    val user = auth.currentUser

    var name by remember { mutableStateOf("Loading...") }
    var email by remember { mutableStateOf(user?.email ?: "") }

    // 🔥 Load user data from Firestore
    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    name = document.getString("name") ?: "No Name"
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(20.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Profile",
            fontSize = 28.sp,
            color = NavText
        )

        // Name
        Text(
            text = "Name: $name",
            fontSize = 18.sp,
            color = NavText,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Email
        Text(
            text = "Email: $email",
            fontSize = 18.sp,
            color = NavText,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Logout button
        Button(
            onClick = {
                auth.signOut()
                navController.navigate(Routes.LOGIN) {
                    popUpTo(Routes.HOME) { inclusive = true }
                    launchSingleTop = true
                }
            },
            modifier = Modifier.padding(top = 32.dp)
        ) {
            Text("Log Out")
        }
    }
}