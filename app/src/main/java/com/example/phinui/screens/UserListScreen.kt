package com.example.phinui.screens

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.phinui.components.messages.User
import com.example.phinui.ui.theme.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.phinui.components.people.BlockFriendDialog
import com.example.phinui.components.people.BlockUserDialog
import com.example.phinui.components.people.RemoveFriendDialog
import com.example.phinui.components.people.SendFriendRequestDialog
import com.example.phinui.data.friends.FriendRepository
import com.example.phinui.viewmodel.ChatRepositoryViewModel
import com.example.phinui.viewmodel.FriendRepositoryViewModel
import com.example.phinui.viewmodel.FriendRepositoryViewModelFactory
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import com.example.phinui.ui.components.UserAvatar


@Composable
fun UserListScreen (
    navController: NavController,
    initialTab: Int = 0,
    chatRepositoryViewModel: ChatRepositoryViewModel = viewModel(),
    friendRepositoryViewModel: FriendRepositoryViewModel = viewModel(
        factory = FriendRepositoryViewModelFactory(FriendRepository())
    )) {

    val currentUserID = chatRepositoryViewModel.currentUserID ?: return
    val friendList = friendRepositoryViewModel.friendsList.value
    var selectedTab by rememberSaveable { mutableIntStateOf(initialTab) }

    var userToAdd by remember { mutableStateOf<Pair<String, String>?>(null) }
    var userToBlock by remember { mutableStateOf<Pair<String, String>?>(null) }
    var friendToRemove by remember { mutableStateOf<Pair<String, String>?>(null) }
    var friendToBlock by remember { mutableStateOf<Pair<String, String>?>(null) }

    val friendRepository = remember { FriendRepository() }
    var myName by remember { mutableStateOf("") }
    val usersDataBase = chatRepositoryViewModel.firebaseFirestoreAuthenticated
    var message by remember { mutableStateOf<String?>(null) }

    var showDeleteDialog = remember { mutableStateOf(false) }
    var selectedChatID = remember { mutableStateOf<String?>(null) }


    LaunchedEffect(Unit) {
        usersDataBase.collection("users").document(currentUserID).get()
            .addOnSuccessListener {
                myName = it.getString("name") ?: ""
            }
    }

    LaunchedEffect(currentUserID){
        chatRepositoryViewModel.loadMessageRequest(currentUserID)
    }

    LaunchedEffect(currentUserID){
        chatRepositoryViewModel.loadApprovedChats(currentUserID)
    }

    LaunchedEffect(currentUserID) {
        chatRepositoryViewModel.userListRepository.loadCurrentUserBlockedListListener(currentUserID)
        chatRepositoryViewModel.userListRepository.loadBlockedByOtherUsersListListener(currentUserID)
    }

    val currentUserBlockedList = chatRepositoryViewModel.userListRepository.currentUserBlockedList.value
    val blockedByOtherUsersList = chatRepositoryViewModel.userListRepository.blockedByOtherUsersList.value
    val hideBlockedUsers = currentUserBlockedList + blockedByOtherUsersList

    if (showDeleteDialog.value && selectedChatID.value != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog.value = false
                selectedChatID.value = null
            },
            title = { Text("Delete chat?") },
            text = { Text("This chat will be deleted from your current chat list.") },
            confirmButton = {
                TextButton(onClick = {
                    chatRepositoryViewModel.onDeleteChat(
                        userID = currentUserID,
                        chatID = selectedChatID.value!!
                    )
                    showDeleteDialog.value = false
                    selectedChatID.value = null
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog.value = false
                        selectedChatID.value = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
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
                val friendChats = chatRepositoryViewModel.getFriendChats.value
                val userCache = remember { mutableStateOf<Map<String, User>>(emptyMap()) }

                val sortedFriendChats = getSortedChats(
                    approvedChatsState = friendChats,
                    chatRepositoryViewModel = chatRepositoryViewModel,
                    userCache = userCache,
                    currentUserID = currentUserID
                )

                Box {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // changed Friends Tab to be chats based instead of users based
                        items(sortedFriendChats) { chat ->
                            val chatID = chat["chatID"] as String
                            val participants = chat["participants"] as List<String>
                            val friendId = participants.first { it != currentUserID }

                            if(friendId in hideBlockedUsers) return@items

                            val friendUser = friendList.first { it.uid == friendId }

                            UserListItem(
                                user = friendUser,
                                trailingContent = {
                                    var showMenu by remember { mutableStateOf(false) }
                                    Box {
                                        IconButton(
                                            onClick = { showMenu = !showMenu }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = "Options",
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
                                                        Text("Remove Friend", color = NavText)
                                                    }
                                                },
                                                onClick = {
                                                    showMenu = false
                                                    friendToRemove = friendId to friendUser.name
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
                                                    friendToBlock = friendId to friendUser.name
                                                }
                                            )

//                                            DropdownMenuItem(
//                                                text = {
//                                                    Row(verticalAlignment = Alignment.CenterVertically) {
//                                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = HeaderRed)
//                                                        Spacer(modifier = Modifier.width(8.dp))
//                                                        Text("Delete", color = HeaderRed)
//                                                    }
//                                                },
//                                                onClick = {
//                                                    showMenu = false
//                                                    //chatRepositoryViewModel.denyRequest(chatID)
//                                                }
//                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    navController.navigate(Routes.MESSAGES + "/${friendId}")
                                },
                                onLongPress = {
                                    selectedChatID.value = chatID
                                    showDeleteDialog.value = true
                                }
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                        }
                    }
                }
            }

            //General tab
            1 -> {
                val approvedChatsState = chatRepositoryViewModel.getGeneralChats.value
                val userCache = remember { mutableStateOf<Map<String, User>>(emptyMap()) }

                val alphabetizedChats = getSortedChats(
                    approvedChatsState = approvedChatsState,
                    chatRepositoryViewModel = chatRepositoryViewModel,
                    userCache = userCache,
                    currentUserID = currentUserID
                )
                Box {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(alphabetizedChats) { chat ->
                            val chatID = chat["chatID"] as? String ?: return@items

                            val participants = chat["participants"] as? List<*> ?: return@items

                            val otherUserID = participants
                                .mapNotNull { it as? String }
                                .firstOrNull{ it != currentUserID } ?:return@items

                            if(otherUserID in hideBlockedUsers) return@items

                            val user = userCache.value[otherUserID] ?: User(
                                uid = otherUserID,
                                name = "Loading Chat...",
                                photoUrl = null
                            )

                            UserListItem(
                                user = user,
                                trailingContent = {
                                    var showMenu by remember { mutableStateOf(false) }
                                    Box {
                                        IconButton(
                                            onClick = { showMenu = !showMenu }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = "Options",
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
                                                        Text("Add Friend", color = NavText)
                                                    }
                                                },
                                                onClick = {
                                                    showMenu = false
                                                    userToAdd = otherUserID to user.name

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
                                                    userToBlock = otherUserID to user.name
                                                }
                                            )

//                                            DropdownMenuItem(
//                                                text = {
//                                                    Row(verticalAlignment = Alignment.CenterVertically) {
//                                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = HeaderRed)
//                                                        Spacer(modifier = Modifier.width(8.dp))
//                                                        Text("Delete", color = HeaderRed)
//                                                    }
//                                                },
//                                                onClick = {
//                                                    showMenu = false
//                                                    //chatRepositoryViewModel.denyRequest(chatID)
//                                                }
//                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    navController.navigate(Routes.MESSAGES + "/${user.uid}")
                                },
                                onLongPress = {
                                    selectedChatID.value = chatID
                                    showDeleteDialog.value = true
                                }
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                        }
                    }
                }
            }

            //Requests tab
            2 -> {
                val messageRequestState = chatRepositoryViewModel.messageRequests.value

                val userCache = remember { mutableStateOf<Map<String, User>>(emptyMap()) }

                val alphabetizedChats = getSortedChats(
                    approvedChatsState = messageRequestState,
                    chatRepositoryViewModel = chatRepositoryViewModel,
                    userCache = userCache,
                    currentUserID = currentUserID
                )

                Box {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(alphabetizedChats) { chat ->
                            val chatID = chat["chatID"] as? String ?: return@items
                            val participants = chat["participants"] as? List<*> ?: return@items

                            val otherUserID = participants
                                .mapNotNull { it as? String }
                                .firstOrNull{ it != currentUserID } ?:return@items

                            val user = userCache.value[otherUserID] ?: User(
                                uid = otherUserID,
                                name = "Loading Request...",
                                photoUrl = null
                            )

                            UserListItem(
                                user = user,
                                trailingContent = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {

                                        IconButton(
                                            onClick = {
                                                chatRepositoryViewModel.approveRequest(
                                                    chatID
                                                )
                                            }
                                        ) {

                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Accept Request",
                                                tint = NavText
                                            )
                                        }

                                        IconButton(
                                            onClick = { chatRepositoryViewModel.denyRequest(chatID) }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Decline Request",
                                                tint = HeaderRed
                                            )
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

    userToAdd?.let { (uid, name) ->
        SendFriendRequestDialog(
            name = name,
            onConfirm = {
                friendRepository.sendFriendRequest(
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

    userToBlock?.let { (uid, name) ->
        BlockUserDialog(
            name = name,
            isFriend = false,
            onConfirm = {
                friendRepository.blockUser(
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

    friendToRemove?.let { (uid, name) ->
        RemoveFriendDialog(
            name = name,
            onConfirm = {
                friendRepository.removeFriend(
                    friendUid = uid,
                    onSuccess = {
                        friendToRemove = null
                    },
                    onError = { e ->
                        message = e.message
                        friendToRemove = null
                    }
                )
            },
            onDismiss = { friendToRemove = null }
        )
    }

    friendToBlock?.let { (uid, name) ->
        BlockFriendDialog(
            name = name,
            onConfirm = {
                friendRepository.blockUser(
                    blockedUid = uid,
                    onSuccess = {
                        friendToBlock = null
                    },
                    onError = { e ->
                        message = e.message
                        friendToBlock = null
                    }
                )
            },
            onDismiss = { friendToBlock = null }
        )
    }

}

@Composable
fun UserListItem(
    user: User,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = { onClick?.invoke() },
                onLongClick = { onLongPress?.invoke() }
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(
                    name = user.name,
                    photoUrl = user.photoUrl,
                    size = 40
                )

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

@Composable
fun getSortedChats(
    approvedChatsState: List<Map<String, Any>>,
    chatRepositoryViewModel: ChatRepositoryViewModel,
    userCache: MutableState<Map<String, User>>,
    currentUserID: String
): List<Map<String, Any>> {
    LaunchedEffect(approvedChatsState) {
        val allUserIds = approvedChatsState.flatMap { chat ->
            val participants =
                chat["participants"] as? List<*> ?: return@flatMap emptyList<String>()
            participants.mapNotNull { it as? String }
        }.distinct()

        val userMap = mutableMapOf<String, User>()

        val deferredResults = allUserIds.map { otherUserID ->
            async {
                try {
                    val userName =
                        chatRepositoryViewModel.userListRepository.getUserNameByID(otherUserID)
                    val photoUrl =
                        chatRepositoryViewModel.userListRepository.getUserPhotoUrlByID(otherUserID)

                    userMap[otherUserID] = User(
                        uid = otherUserID,
                        name = userName,
                        photoUrl = photoUrl
                    )
                } catch (e: Exception) {
                    userMap[otherUserID] = User(
                        uid = otherUserID,
                        name = "Unknown User",
                        photoUrl = null
                    )
                }
            }
        }

        coroutineScope {
            deferredResults.awaitAll()
        }

        userCache.value = userMap
    }

    val sortedChats = approvedChatsState.sortedWith(
        compareBy<Map<String, Any>> { chat ->
            val filteredParticipants = chat["participants"] as? List<*> ?: return@compareBy ""
            val filteredOtherUserID = filteredParticipants
                .mapNotNull { it as? String }
                .firstOrNull { it != currentUserID }

            val userName = userCache.value[filteredOtherUserID]?.name ?: "Unknown User"
            userName.lowercase()
        }.thenBy { chat ->
            chat["chatID"] as? String ?: ""
        }
    )

    return sortedChats
}