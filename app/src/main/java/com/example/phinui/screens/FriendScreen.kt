package com.example.phinui.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.phinui.data.friends.FriendRepository
import com.example.phinui.ui.components.UserAvatar
import com.example.phinui.ui.theme.Background
import com.example.phinui.ui.theme.NavText
import com.google.firebase.firestore.ListenerRegistration

@Composable
fun FriendsScreen() {
    val repo = remember { FriendRepository() }

    var friends by remember { mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList()) }
    var message by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        val friendsListener: ListenerRegistration? =
            repo.listenFriends(
                onResult = { friends = it },
                onError = { message = it.message }
            )

        onDispose {
            friendsListener?.remove()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(20.dp)
    ) {
        Text(
            text = "Friends",
            fontSize = 28.sp,
            color = NavText
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(friends) { (uid, user) ->
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

                    Row {
                        OutlinedButton(
                            onClick = {
                                repo.removeFriend(
                                    friendUid = uid,
                                    onSuccess = {
                                        message = "Friend removed."
                                    },
                                    onError = { e -> message = e.message }
                                )
                            }
                        ) {
                            Text("Remove")
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

        message?.let {
            Text(it, color = NavText)
        }
    }
}