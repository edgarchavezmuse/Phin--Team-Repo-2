package com.example.phinui.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.phinui.data.friends.FriendRepository
import com.example.phinui.ui.components.UserAvatar
import com.example.phinui.ui.theme.Background
import com.example.phinui.ui.theme.HeaderRed
import com.example.phinui.ui.theme.NavText
import com.example.phinui.ui.theme.TextMuted
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import androidx.compose.material.icons.filled.Email
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.phinui.components.people.AlreadyFriendDialog
import com.example.phinui.components.people.BlockUserDialog
import com.example.phinui.components.people.SendFriendRequestDialog
import com.example.phinui.components.people.UnblockUserDialog
import com.example.phinui.viewmodel.ChatRepositoryViewModel

@Composable
fun PeopleScreen() {
    val repo = remember { FriendRepository() }
    val db = remember { FirebaseFirestore.getInstance() }
    val auth = remember { FirebaseAuth.getInstance() }
    val keyboardController = LocalSoftwareKeyboardController.current

    var selectedTab by remember { mutableIntStateOf(0) }

    var search by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList()) }
    var blockedUsers by remember { mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList()) }
    var message by remember { mutableStateOf<String?>(null) }
    var myName by remember { mutableStateOf("") }
    val chatRepositoryViewModel: ChatRepositoryViewModel = viewModel()
    var friends by remember { mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList()) }
    var alreadyFriendUser by remember { mutableStateOf<String?>(null) }

    var userToAdd by remember { mutableStateOf<Pair<String, String>?>(null) }
    var userToBlock by remember { mutableStateOf<Pair<String, String>?>(null) }
    var userToUnblock by remember { mutableStateOf<Pair<String, String>?>(null) }

    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid ?: return@LaunchedEffect
        db.collection("users").document(uid).get()
            .addOnSuccessListener {
                myName = it.getString("name") ?: ""
            }
    }

    LaunchedEffect(Unit) {
        val currentUserID = auth.currentUser?.uid ?: return@LaunchedEffect
        chatRepositoryViewModel.startListening(userID = currentUserID)
    }

    DisposableEffect(Unit) {
        val friendsListener: ListenerRegistration? =
            repo.listenFriends(
                onResult = { friends = it },
                onError = { message = it.message }
            )

        val blockedListener: ListenerRegistration? =
            repo.listenBlockedUsers(
                onResult = { blockedUsers = it },
                onError = { message = it.message }
            )

        onDispose {
            blockedListener?.remove()
            friendsListener?.remove()
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
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "People",
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
                Text("Discover")
            }
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 }
            ) {
                Text("Blocked")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
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

                val visibleSearchResults = searchResults.filter {
                    (uid, _) -> blockedUsers.none {
                        (blockedUid, _) -> blockedUid == uid
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(visibleSearchResults) { (uid, user) ->
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
                                UserAvatar(name, 44)
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
                                        contentDescription = "Person Options",
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
                                                    imageVector = Icons.Default.PersonAdd,
                                                    contentDescription = "Add Friend",
                                                    tint = NavText
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Add Friend")
                                            }
                                        },
                                        onClick = {
                                            showMenu = false

                                            val isFriend = friends.any { (friendUid, _) -> friendUid == uid }

                                            if (isFriend) {
                                                alreadyFriendUser = name
                                            } else {
                                                userToAdd = uid to name
                                            }
                                        }
                                    )

                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Email,
                                                    contentDescription = "Send Message Request",
                                                    tint = NavText
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Send Message Request", color = NavText)
                                            }
                                        },
                                        onClick = {
                                            val senderID = chatRepositoryViewModel.currentUserID ?: return@DropdownMenuItem
                                            chatRepositoryViewModel.sendMessageRequest(
                                                senderID,
                                                uid,
                                                name
                                            )
                                            showMenu = false
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
                                            userToBlock = uid to name
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(blockedUsers) { (uid, user) ->
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
                                UserAvatar(name, 44)
                                Spacer(modifier = Modifier.width(12.dp))

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

                            IconButton(
                                onClick = {
                                    userToUnblock = uid to name
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Unblock User",
                                    tint = HeaderRed
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    userToAdd?.let { (uid, name) ->
        SendFriendRequestDialog(
            name = name,
            onConfirm = {
                repo.sendFriendRequest(
                    uid,
                    myName,
                    {
                        userToAdd = null
                    },
                    {
                        message = it.message
                        userToAdd = null
                    }
                )
            },
            onDismiss = { userToAdd = null }
        )
    }

    alreadyFriendUser?.let { name ->
        AlreadyFriendDialog(
            name = name,
            onDismiss = { alreadyFriendUser = null}
        )

    }

    userToBlock?.let { (uid, name) ->
        val isFriend = friends.any { (friendUid, _) -> friendUid == uid }
        BlockUserDialog(
            name = name,
            isFriend = isFriend,
            onConfirm = {
                repo.blockUser(
                    uid,
                    {
                        userToBlock = null
                    },
                    {
                        message = it.message
                        userToBlock = null
                    }
                )
            },
            onDismiss = { userToBlock = null }
        )
    }

    userToUnblock?.let { (uid, name) ->
        UnblockUserDialog(
            name = name,
            onConfirm = {
                repo.unblockUser(
                    uid,
                    {
                        userToUnblock = null
                    },
                    {
                        message = it.message
                        userToUnblock = null
                    }
                )
            },
            onDismiss = { userToUnblock = null }
        )
    }

    val receiverUserName = chatRepositoryViewModel.activeChatUserName.value ?: "Unknown"
    if (chatRepositoryViewModel.showMessageApprovedDialog.value) {
        AlertDialog(
            onDismissRequest = {
                chatRepositoryViewModel.showMessageApprovedDialog.value = false
            },
            title = {
                Text("Active Chat")
            },
            text = {
                Text("You already have an active chat with $receiverUserName")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        chatRepositoryViewModel.showMessageApprovedDialog.value = false
                    }
                ) {
                    Text("Ok")
                }
            }
        )
    }

    val context = LocalContext.current
    chatRepositoryViewModel.confirmSendMessageRequest.value?.let { confirmSendMessageRequest ->
        Toast.makeText(
            context,
            confirmSendMessageRequest,
            Toast.LENGTH_SHORT
        ).show()
        chatRepositoryViewModel.confirmSendMessageRequest.value = null
    }
}