package com.example.phinui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.lifecycle.viewModelScope
import com.example.phinui.data.friends.FriendRepository
import com.example.phinui.viewmodel.ChatRepositoryViewModel
import com.example.phinui.viewmodel.FriendRepositoryViewModel
import com.example.phinui.viewmodel.FriendRepositoryViewModelFactory
import com.example.phinui.viewmodel.UserListViewModel


@Composable
fun UserListScreen (
    navController: NavController,
    chatRepositoryViewModel: ChatRepositoryViewModel = viewModel(),
    friendRepositoryViewModel: FriendRepositoryViewModel = viewModel(
        factory = FriendRepositoryViewModelFactory(FriendRepository())
    )) {

    val messageRequest = chatRepositoryViewModel.messageRequests
    val currentUserID = chatRepositoryViewModel.currentUserID ?: return
    val friendList = friendRepositoryViewModel.friendsList.value
    var selectedTab by remember { mutableIntStateOf(value = 0) }

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
                Text("General")
            }
            Tab(selected = selectedTab == 2,
                onClick = {selectedTab = 2}) {
                Text("Requests")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when(selectedTab) {

            //Friends tab
            0 -> {
                Box {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(friendList) { friend ->
                            UserListItem(
                                user = friend,
                                onClick = {
                                    navController.navigate(Routes.MESSAGES + "/${friend.uid}")
                                }
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                        }
                    }
                }
            }

            //General tab

            //Requests tab
            2 -> {
                Box {
                    LaunchedEffect(Unit) {
                        val uid = chatRepositoryViewModel.currentUserID ?: return@LaunchedEffect
                        chatRepositoryViewModel.loadMessageRequest(uid)
                    }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(messageRequest.value) { chat ->
                            val chatID = chat["chatID"] as? String ?: return@items
                            val participants = chat["participants"] as? List<*> ?: return@items

                            val otherUserID = participants
                                .mapNotNull { it as? String }
                                .firstOrNull{ it != currentUserID } ?:return@items

                            var userName by remember { mutableStateOf("Loading...")}

                            LaunchedEffect(otherUserID) {
                                chatRepositoryViewModel.userListRepository.getUserNameByID(
                                    otherUserID,
                                    onResult = { user ->
                                        userName = user.name
                                    },
                                    onError = {exception ->
                                        userName = "Unknown User"
                                    }
                                )
                            }

                            val user = User(
                                uid = otherUserID,
                                name = userName
                            )

                            UserListItem(
                                user = user,
                                trailingContent = {
                                    Row{
                                        Button(onClick = {
                                            chatRepositoryViewModel.approveRequest(chatID)
                                        }) {
                                            Text("Accept")
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Button(onClick = {
                                            chatRepositoryViewModel.denyRequest(chatID)
                                        }) {
                                            Text("Decline")
                                        }
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable fun UserListItem(
    user: User,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick:(() -> Unit)? = null
) {
    val initial = user.name
        .trim()
        .firstOrNull()
        ?.uppercase() ?: "?"
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .then(
                if (onClick != null) Modifier.clickable {
                    onClick()
                }
                else Modifier
            )
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween

        ) {
            // User Icons
            Row(verticalAlignment = Alignment.CenterVertically) {
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

                Text(
                    text = user.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = TextMuted
                    )
                )
            }
            trailingContent?.invoke()
        }
    }
}