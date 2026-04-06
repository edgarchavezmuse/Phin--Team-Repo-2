package com.example.phinui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.*
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.phinui.ui.navigation.Routes
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.credentials.exceptions.domerrors.InvalidModificationError
import com.example.phinui.components.messages.UserListRepository
import com.example.phinui.components.messages.User
import com.example.phinui.ui.theme.*


@Composable fun UserListScreen (navController: NavController) {
    val currentUserID = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val userListRepository = remember { UserListRepository() }

    var users by remember { mutableStateOf(listOf<User>()) }
    var isLoadingStatus by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        users = userListRepository.getAllUsers(currentUserID)
        isLoadingStatus = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(top = 24.dp)
    ) {
        if (isLoadingStatus) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Text(
                        text = "Users",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NavText
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }

                items(users) { user ->
                    UserListItem(user = user, navController = navController)
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable fun UserListItem(user: User, navController: NavController) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable{
                navController.navigate(Routes.MESSAGES + "/${user.uid}")
            }
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // User Icons
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(50))
                    .background(PrimaryRed)
            ) {
                // Placeholder for user icons
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = TextMuted
                    )
                )
            }
        }
    }
}