package com.example.phinui.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.phinui.data.friends.FriendRepository
import com.example.phinui.data.friends.FriendRequest
import com.example.phinui.ui.components.UserAvatar
import com.example.phinui.ui.navigation.Routes
import com.example.phinui.ui.theme.Background
import com.example.phinui.ui.theme.HeaderRed
import com.example.phinui.ui.theme.HeaderText
import com.example.phinui.ui.theme.NavText
import com.google.firebase.firestore.ListenerRegistration

@Composable
fun FriendsScreen(
    navController: NavController
) {
    val repo = remember { FriendRepository() }

    var selectedTab by remember { mutableIntStateOf(0) }
    var friends by remember { mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList()) }
    var incoming by remember { mutableStateOf<List<Pair<String, FriendRequest>>>(emptyList()) }
    var message by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        val friendsListener: ListenerRegistration? =
            repo.listenFriends(
                onResult = { friends = it },
                onError = { message = it.message }
            )

        val incomingListener: ListenerRegistration? =
            repo.listenIncomingRequests(
                onResult = { incoming = it },
                onError = { message = it.message }
            )

        onDispose {
            friendsListener?.remove()
            incomingListener?.remove()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = "Friends",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NavText
                )

                Spacer(modifier = Modifier.height(16.dp))

                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 }
                    ) {
                        Text("Friends")
                    }

                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 }
                    ) {
                        Text("Requests")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (selectedTab == 0) {
                items(friends) { (uid, user) ->
                    val name = user["name"] as? String ?: "Unknown"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            UserAvatar(name = name, size = 44)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = name,
                                color = NavText
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    repo.removeFriend(
                                        friendUid = uid,
                                        onSuccess = {
                                            message = "Friend removed."
                                        },
                                        onError = { e ->
                                            message = e.message
                                        }
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PersonRemove,
                                    contentDescription = "Remove Friend",
                                    tint = HeaderRed
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            IconButton(
                                onClick = {
                                    repo.blockUser(
                                        blockedUid = uid,
                                        onSuccess = {
                                            message = "User blocked."
                                        },
                                        onError = { e ->
                                            message = e.message
                                        }
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Block,
                                    contentDescription = "Block User",
                                    tint = HeaderRed
                                )
                            }
                        }
                    }
                }
            }

            if (selectedTab == 1) {
                items(incoming) { (requestId, req) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            UserAvatar(req.fromName, 44)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = req.fromName,
                                color = NavText
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    repo.acceptFriendRequest(
                                        requestId = requestId,
                                        fromUid = req.fromUid,
                                        onSuccess = {
                                            message = "Friend added."
                                        },
                                        onError = { e ->
                                            message = e.message
                                        }
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Accept Request",
                                    tint = HeaderRed
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            IconButton(
                                onClick = {
                                    repo.declineFriendRequest(
                                        requestId = requestId,
                                        onSuccess = {
                                            message = "Declined."
                                        },
                                        onError = { e ->
                                            message = e.message
                                        }
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Decline Request",
                                    tint = HeaderRed
                                )
                            }
                        }
                    }
                }
            }

            message?.let {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = NavText
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = {
                navController.navigate(Routes.PEOPLE) {
                    launchSingleTop = true
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(4.dp),
            containerColor = HeaderRed,
            contentColor = HeaderText
        ) {
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = "Open People"
            )
        }
    }
}