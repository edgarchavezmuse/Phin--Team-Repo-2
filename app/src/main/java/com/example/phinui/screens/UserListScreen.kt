package com.example.phinui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.phinui.components.messages.User
import com.example.phinui.ui.theme.*
import com.example.phinui.viewmodel.UserListViewModel
import androidx.compose.foundation.shape.CircleShape
import kotlinx.coroutines.selects.select


@Composable fun UserListScreen (navController: NavController, viewModel: UserListViewModel = viewModel()) {
    val users = viewModel.sortedUsers
    val isLoadingStatus = viewModel.isLoading
    var selectedTab by remember { mutableIntStateOf(value = 0) }

    LaunchedEffect(Unit) {
        viewModel.loadAllUsers()
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0,
                onClick = {selectedTab = 0}) {
                    Text("Friends")
            }
            Tab(selected = selectedTab == 1,
                onClick = {selectedTab = 1}) {
                    Text("Requests")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when(selectedTab) {

            //Friends tab
            0 -> {
                Box {
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
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            //item {
                            //    Text(
                            //        text = "Users",
                            //        fontSize = 28.sp,
                            //        fontWeight = FontWeight.SemiBold,
                            //        color = NavText
                            //    )
                            //}

                            //item {
                            //    Spacer(modifier = Modifier.height(24.dp))
                            //}

                            items(users) { user ->
                                UserListItem(user = user, navController = navController)
                                Spacer(modifier = Modifier.height(5.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable fun UserListItem(user: User, navController: NavController) {
    val initial = user.name
        .trim()
        .firstOrNull()
        ?.uppercase() ?: "?"
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
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically

        ) {
            // User Icons
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFEFEF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFD32F2F),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
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