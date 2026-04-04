package com.example.phinui.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.phinui.data.friends.FriendRepository
import com.example.phinui.data.friends.FriendRequest
import com.example.phinui.ui.components.UserAvatar
import com.example.phinui.ui.theme.Background
import com.example.phinui.ui.theme.NavText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

@Composable
fun PeopleScreen() {
    val repo = remember { FriendRepository() }
    val db = remember { FirebaseFirestore.getInstance() }
    val auth = remember { FirebaseAuth.getInstance() }

    var selectedTab by remember { mutableIntStateOf(0) }

    var search by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList()) }
    var incoming by remember { mutableStateOf<List<Pair<String, FriendRequest>>>(emptyList()) }
    var blockedUsers by remember { mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList()) }
    var message by remember { mutableStateOf<String?>(null) }
    var myName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid ?: return@LaunchedEffect

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                myName = doc.getString("name") ?: ""
            }
    }

    DisposableEffect(Unit) {
        val incomingListener: ListenerRegistration? =
            repo.listenIncomingRequests(
                onResult = { incoming = it },
                onError = { message = it.message }
            )

        val blockedListener: ListenerRegistration? =
            repo.listenBlockedUsers(
                onResult = { blockedUsers = it },
                onError = { message = it.message }
            )

        onDispose {
            incomingListener?.remove()
            blockedListener?.remove()
        }
    }

    DisposableEffect(search) {
        val searchListener =
            repo.listenSearchUsersByNamePrefix(
                query = search,
                onResult = { searchResults = it },
                onError = { message = it.message }
            )

        onDispose {
            searchListener?.remove()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(20.dp)
    ) {
        Text(
            text = "People",
            fontSize = 28.sp,
            color = NavText
        )

        Spacer(modifier = Modifier.height(16.dp))

        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("People") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Blocked") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 0) {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                label = { Text("Search users by name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Search Results", color = NavText)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                items(searchResults) { (uid, user) ->
                    val name = user["name"] as? String ?: "Unknown"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            UserAvatar(name = name, size = 40)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = name,
                                color = NavText
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    repo.sendFriendRequest(
                                        toUid = uid,
                                        fromName = myName,
                                        onSuccess = { message = "Request sent." },
                                        onError = { e -> message = e.message }
                                    )
                                }
                            ) {
                                Text("Add")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            OutlinedButton(
                                onClick = {
                                    repo.blockUser(
                                        blockedUid = uid,
                                        onSuccess = {
                                            message = "User blocked."
                                        },
                                        onError = { e -> message = e.message }
                                    )
                                }
                            ) {
                                Text("Block")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Incoming Requests", color = NavText)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(incoming) { (requestId, req) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            UserAvatar(name = req.fromName, size = 40)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = req.fromName,
                                color = NavText
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    repo.acceptFriendRequest(
                                        requestId = requestId,
                                        fromUid = req.fromUid,
                                        onSuccess = {
                                            message = "Friend added."
                                        },
                                        onError = { e -> message = e.message }
                                    )
                                }
                            ) {
                                Text("Accept")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            OutlinedButton(
                                onClick = {
                                    repo.declineFriendRequest(
                                        requestId = requestId,
                                        onSuccess = {
                                            message = "Request declined."
                                        },
                                        onError = { e -> message = e.message }
                                    )
                                }
                            ) {
                                Text("Decline")
                            }
                        }
                    }
                }
            }
        } else {
            Text("Blocked Users", color = NavText)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(blockedUsers) { (uid, user) ->
                    val name = user["name"] as? String ?: "Unknown"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            UserAvatar(name = name, size = 40)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = name,
                                color = NavText
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                repo.unblockUser(
                                    blockedUid = uid,
                                    onSuccess = {
                                        message = "User unblocked."
                                    },
                                    onError = { e -> message = e.message }
                                )
                            }
                        ) {
                            Text("Unblock")
                        }
                    }
                }
            }
        }

        message?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(it, color = NavText)
        }
    }
}