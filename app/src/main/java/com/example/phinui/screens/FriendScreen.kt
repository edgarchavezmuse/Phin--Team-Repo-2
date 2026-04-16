package com.example.phinui.ui.screens

import android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.phinui.ui.theme.TextMuted
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Block
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle

@Composable
fun FriendsScreen(
    navController: NavController
) {
    val repo = remember { FriendRepository() }
    val db = remember { FirebaseFirestore.getInstance() }

    var selectedTab by remember { mutableIntStateOf(0) }
    var friends by remember { mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList()) }
    var incoming by remember { mutableStateOf<List<Pair<String, FriendRequest>>>(emptyList()) }
    var message by remember { mutableStateOf<String?>(null) }
    var requestEmails by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    var friendToRemove by remember { mutableStateOf<Pair<String, String>?>(null) }
    var friendToBlock by remember { mutableStateOf<Pair<String, String>?>(null) }

    var requestToAccept by remember { mutableStateOf<Triple<String, String, String>?>(null) }
    var requestToDecline by remember { mutableStateOf<Pair<String, String>?>(null) }

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

    LaunchedEffect(incoming) {
        incoming.forEach { (_, req) ->
            if (!requestEmails.containsKey(req.fromUid)) {
                db.collection("users")
                    .document(req.fromUid)
                    .get()
                    .addOnSuccessListener { doc ->
                        val email = doc.getString("email") ?: ""
                        requestEmails = requestEmails + (req.fromUid to email)
                    }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = 8.dp, vertical = 16.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp),
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
                    val email = user["email"] as? String ?: ""

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
                            Spacer(modifier = Modifier.width(8.dp))

                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = name,
                                    color = NavText
                                )

                                Text(
                                    text = email,
                                    color = TextMuted,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }
                        }

                        var showMenu by remember { mutableStateOf(false) }

                        Box {
                            IconButton(
                                onClick = { showMenu = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Friend Options",
                                    tint = NavText
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                containerColor = Background
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.PersonRemove,
                                                contentDescription = "Remove Friend",
                                                tint = NavText
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Remove Friend")
                                        }
                                    },
                                    onClick = {
                                        showMenu = false
                                        friendToRemove = uid to name
                                    }
                                )

                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Block,
                                                contentDescription = "Block User",
                                                tint = HeaderRed
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Block User", color = HeaderRed)
                                        }
                                    },
                                    onClick = {
                                        showMenu = false
                                        friendToBlock = uid to name
                                    }
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
                            Spacer(modifier = Modifier.width(8.dp))

                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = req.fromName,
                                    color = NavText
                                )

                                Text(
                                    text = requestEmails[req.fromUid] ?: "",
                                    color = TextMuted,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    requestToAccept = Triple(requestId, req.fromUid, req.fromName)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Accept Request",
                                    tint = NavText
                                )
                            }

                            Spacer(modifier = Modifier.width(2.dp))

                            IconButton(
                                onClick = {
                                    requestToDecline = requestId to req.fromName
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

    friendToRemove?.let { (uid, name) ->
        AlertDialog(
            onDismissRequest = { friendToRemove = null },
            containerColor = Background,
            title = {
                Text("Remove friend?")
            },
            text = {
                Text(
                    buildAnnotatedString {
                        append("Are you sure you want to remove ")

                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(name)
                        }
                        append(" from your friends?")
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        repo.removeFriend(
                            friendUid = uid,
                            onSuccess = {
                                friendToRemove = null
                            },
                            onError = { e ->
                                message = e.message
                                friendToRemove = null
                            }
                        )
                    }
                ) {
                    Text("Remove", color = HeaderRed)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { friendToRemove = null }
                ) {
                    Text("Cancel", color = NavText)
                }
            }
        )
    }

    friendToBlock?.let { (uid, name) ->
        AlertDialog(
            onDismissRequest = { friendToBlock = null },
            containerColor = Background,
            title = {
                Text("Block user?")
            },
            text = {
                Text(
                    buildAnnotatedString {
                        append("Are you sure you want to block ")

                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(name)
                        }
                        append("? This will also remove them from your friends.")
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        repo.blockUser(
                            blockedUid = uid,
                            onSuccess = {
                                friendToBlock = null
                            },
                            onError = { e ->
                                message = e.message
                                friendToBlock = null
                            }
                        )
                    }
                ) {
                    Text("Block", color = HeaderRed)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { friendToBlock = null }
                ) {
                    Text("Cancel", color = NavText)
                }
            }
        )
    }

    requestToAccept?.let { (requestId, fromUid, name) ->
        AlertDialog(
            onDismissRequest = { requestToAccept = null },
            containerColor = Background,
            title = {
                Text("Accept request?")
            },
            text = {
                Text(
                    buildAnnotatedString {
                        append("Do you want to accept the friend request from ")

                        withStyle(
                            style = SpanStyle(fontWeight = FontWeight.Bold)
                        ) {
                            append(name)
                        }

                        append("?")
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        repo.acceptFriendRequest(
                            requestId = requestId,
                            fromUid = fromUid,
                            onSuccess = {
                                requestToAccept = null
                            },
                            onError = { e ->
                                message = e.message
                                requestToAccept = null
                            }
                        )
                    }
                ) {
                    Text("Accept", color = HeaderRed)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { requestToAccept = null }
                ) {
                    Text("Cancel", color = NavText)
                }
            }
        )
    }

    requestToDecline?.let { (requestId, name) ->
        AlertDialog(
            onDismissRequest = { requestToDecline = null },
            containerColor = Background,
            title = {
                Text("Decline request?")
            },
            text = {
                Text(
                    buildAnnotatedString {
                        append("Are you sure you want to decline the friend request from ")

                        withStyle(
                            style = SpanStyle(fontWeight = FontWeight.Bold)
                        ) {
                            append(name)
                        }

                        append("?")
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        repo.declineFriendRequest(
                            requestId = requestId,
                            onSuccess = {
                                requestToDecline = null
                            },
                            onError = { e ->
                                message = e.message
                                requestToDecline = null
                            }
                        )
                    }
                ) {
                    Text("Decline", color = HeaderRed)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { requestToDecline = null }
                ) {
                    Text("Cancel", color = NavText)
                }
            }
        )
    }
}