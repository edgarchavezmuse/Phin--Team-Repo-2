package com.example.phinui.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
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
    val keyboardController = LocalSoftwareKeyboardController.current

    var selectedTab by remember { mutableIntStateOf(0) }

    var search by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList()) }
    var incoming by remember { mutableStateOf<List<Pair<String, FriendRequest>>>(emptyList()) }
    var blockedUsers by remember { mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList()) }
    var message by remember { mutableStateOf<String?>(null) }
    var myName by remember { mutableStateOf("") }

    // load current user's name
    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid ?: return@LaunchedEffect
        db.collection("users").document(uid).get()
            .addOnSuccessListener {
                myName = it.getString("name") ?: ""
            }
    }

    // realtime listeners
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

    // search listener
    DisposableEffect(search) {
        val searchListener =
            repo.listenSearchUsersByNamePrefix(
                query = search,
                onResult = { searchResults = it },
                onError = { message = it.message }
            )

        onDispose { searchListener?.remove() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("People", fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = NavText)

        Spacer(modifier = Modifier.height(16.dp))

        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("People")
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("Requests")
            }
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                Text("Blocked")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {

            // people tab
            0 -> {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    label = { Text("Search users by name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = { keyboardController?.hide() }
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(searchResults) { (uid, user) ->
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
                                UserAvatar(name, 44)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(name, color = NavText)
                            }

                            Row {
                                Button(
                                    onClick = {
                                        repo.sendFriendRequest(
                                            uid,
                                            myName,
                                            { message = "Request sent" },
                                            { message = it.message }
                                        )
                                    }
                                ) { Text("Add") }

                                Spacer(modifier = Modifier.width(8.dp))

                                OutlinedButton(
                                    onClick = {
                                        repo.blockUser(
                                            uid,
                                            { message = "User blocked" },
                                            { message = it.message }
                                        )
                                    }
                                ) { Text("Block") }
                            }
                        }
                    }
                }
            }

            // requests tab
            1 -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
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
                                Text(req.fromName, color = NavText)
                            }

                            Row {
                                Button(
                                    onClick = {
                                        repo.acceptFriendRequest(
                                            requestId,
                                            req.fromUid,
                                            { message = "Friend added" },
                                            { message = it.message }
                                        )
                                    }
                                ) { Text("Accept") }

                                Spacer(modifier = Modifier.width(8.dp))

                                OutlinedButton(
                                    onClick = {
                                        repo.declineFriendRequest(
                                            requestId,
                                            { message = "Declined" },
                                            { message = it.message }
                                        )
                                    }
                                ) { Text("Decline") }
                            }
                        }
                    }
                }
            }

            // blocked tab
            2 -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(blockedUsers) { (uid, user) ->
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
                                UserAvatar(name, 44)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(name, color = NavText)
                            }

                            OutlinedButton(
                                onClick = {
                                    repo.unblockUser(
                                        uid,
                                        { message = "Unblocked" },
                                        { message = it.message }
                                    )
                                }
                            ) {
                                Text("Unblock")
                            }
                        }
                    }
                }
            }
        }

        message?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = NavText)
        }
    }
}