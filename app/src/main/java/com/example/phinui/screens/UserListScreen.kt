package com.example.phinui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.example.phinui.ui.navigation.Routes
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.phinui.components.messages.User
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
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
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.ui.text.style.TextAlign


@OptIn(ExperimentalMaterial3Api::class)
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

    var selectedChatID by remember { mutableStateOf<String?>(null) }
    var showActionSheet by remember { mutableStateOf(false) }
    val mutedChats by chatRepositoryViewModel.mutedChats.collectAsState()

    var showCreateFriendGroupDialog by remember { mutableStateOf(false) }


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

    val messageRequestsCountState = chatRepositoryViewModel.messageRequests.value

    val userCountCache = remember { mutableStateOf<Map<String, User>>(emptyMap()) }

    val sortedCountChats = getSortedChats(
        approvedChatsState = messageRequestsCountState,
        chatRepositoryViewModel = chatRepositoryViewModel,
        userCache = userCountCache,
        currentUserID = currentUserID
    )

    val totalMessageRequestCount = sortedCountChats.size

    val friendMessagesCount = chatRepositoryViewModel.getFriendChats.value
    val userCacheCount = remember { mutableStateOf<Map<String, User>>(emptyMap()) }

    val generalMessagesStateCount = chatRepositoryViewModel.getGeneralChats.value

    val sortedFriendMessagesCount = getSortedChats(
        approvedChatsState = friendMessagesCount,
        chatRepositoryViewModel = chatRepositoryViewModel,
        userCache = userCacheCount,
        currentUserID = currentUserID
    )

    val sortedGeneralMessagesStateCount = getSortedChats(
        approvedChatsState = generalMessagesStateCount,
        chatRepositoryViewModel = chatRepositoryViewModel,
        userCache = userCacheCount,
        currentUserID = currentUserID
    )

    val totalFriendsMessagesCount = sortedFriendMessagesCount.sumOf { chat ->
        val unreadFriendsMessagesCount = chat["unreadCounts"] as? Map<String, Long> ?: emptyMap()

        unreadFriendsMessagesCount[currentUserID]?.toInt() ?: 0
    }

    val totalGeneralMessagesCount = sortedGeneralMessagesStateCount.sumOf { chat ->
        val unreadGeneralMessagesCount = chat["unreadCounts"] as? Map<String, Long> ?: emptyMap()

        unreadGeneralMessagesCount[currentUserID]?.toInt() ?: 0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 }) {
                if (totalFriendsMessagesCount > 0) {
                    Text("Friends (${totalFriendsMessagesCount})")
                } else {
                    Text("Friends")
                }
            }
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 }) {
                if (totalGeneralMessagesCount > 0) {
                    Text("General (${totalGeneralMessagesCount})")
                } else {
                    Text("General")
                }
            }
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 }) {
                if (totalMessageRequestCount > 0) {
                    Text(
                        "Requests (${totalMessageRequestCount})"
                    )
                } else {
                    Text("Requests")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {

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

                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (sortedFriendChats.isEmpty()) {
                        EmptyMessagesState(
                            title = "No friend chats yet",
                            subtitle = "Start a conversation with a friend or create a group chat.",
                            icon = Icons.Default.Group
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp),
                        ) {
                            // changed Friends Tab to be chats based instead of users based
                            items(sortedFriendChats) { chat ->
                                val chatID = chat["chatID"] as String
                                val participants = chat["participants"] as List<String>
                                val chatType = chat["type"] as? String ?: "direct"

                                val unreadCounts =
                                    chat["unreadCounts"] as? Map<String, Long> ?: emptyMap()
                                val unreadCount = unreadCounts[currentUserID]?.toInt() ?: 0

                                val lastMessage = chat["lastMessage"] as? String ?: ""
                                val timestamp = chat["lastTimestamp"] as? Timestamp

                                val previewText = if (lastMessage.isBlank()) {
                                    "Start conversation"
                                } else {
                                    lastMessage
                                }

                                if (chatType == "group") {
                                    val groupName = chat["groupName"] as? String ?: "Group Chat"

                                    UserListItem(
                                        user = User(
                                            uid = chatID,
                                            name = groupName,
                                            photoUrl = null
                                        ),
                                        chatID = chatID,
                                        mutedChats = mutedChats,
                                        unreadCount = unreadCount,
                                        subtitle = previewText,
                                        timeText = formatTimestamp(timestamp),
                                        onClick = {
                                            navController.navigate(
                                                Routes.groupMessagesRoute(
                                                    chatID = chatID,
                                                    groupName = groupName
                                                )
                                            )
                                        },
                                        onLongPress = {
                                            selectedChatID = chatID
                                            showActionSheet = true
                                        }
                                    )
                                } else {
                                    val friendId = participants.first { it != currentUserID }

                                    if (friendId in hideBlockedUsers) return@items

                                    val friendUser =
                                        friendList.firstOrNull { it.uid == friendId } ?: User(
                                            uid = friendId,
                                            name = "Loading...",
                                            photoUrl = null
                                        )

                                    UserListItem(
                                        user = friendUser,
                                        chatID = chatID,
                                        mutedChats = mutedChats,
                                        unreadCount = unreadCount,
                                        subtitle = previewText,
                                        timeText = formatTimestamp(timestamp),
                                        trailingContent = {
                                            var showMenu by remember { mutableStateOf(false) }

                                            Box {
                                                IconButton(onClick = { showMenu = !showMenu }) {
                                                    Icon(
                                                        imageVector = Icons.Default.MoreVert,
                                                        contentDescription = "Options",
                                                        tint = MaterialTheme.colorScheme.onTertiary
                                                    )
                                                }

                                                DropdownMenu(
                                                    expanded = showMenu,
                                                    onDismissRequest = { showMenu = false },
                                                    containerColor = MaterialTheme.colorScheme.surface
                                                ) {
                                                    DropdownMenuItem(
                                                        text = {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Icon(
                                                                    imageVector = Icons.Default.PersonRemove,
                                                                    contentDescription = "Remove Friend",
                                                                    tint = MaterialTheme.colorScheme.onTertiary
                                                                )
                                                                Spacer(modifier = Modifier.width(8.dp))
                                                                Text(
                                                                    "Remove Friend",
                                                                    color = MaterialTheme.colorScheme.onTertiary
                                                                )
                                                            }
                                                        },
                                                        onClick = {
                                                            showMenu = false
                                                            friendToRemove =
                                                                friendId to friendUser.name
                                                        }
                                                    )

                                                    DropdownMenuItem(
                                                        text = {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Block,
                                                                    contentDescription = "Block User",
                                                                    tint = MaterialTheme.colorScheme.primary
                                                                )
                                                                Spacer(modifier = Modifier.width(8.dp))
                                                                Text(
                                                                    "Block User",
                                                                    color = MaterialTheme.colorScheme.primary
                                                                )
                                                            }
                                                        },
                                                        onClick = {
                                                            showMenu = false
                                                            friendToBlock =
                                                                friendId to friendUser.name
                                                        }
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            navController.navigate(Routes.MESSAGES + "/${friendId}")
                                        },
                                        onLongPress = {
                                            selectedChatID = chatID
                                            showActionSheet = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                    FloatingActionButton(
                        onClick = { showCreateFriendGroupDialog = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create group chat"
                        )
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

                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (alphabetizedChats.isEmpty()) {
                        EmptyMessagesState(
                            title = "No general chats yet",
                            subtitle = "Messages from people who are not friends will appear here.",
                            icon = Icons.Default.ChatBubbleOutline
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp),
                        ) {
                            items(alphabetizedChats) { chat ->
                                val chatID = chat["chatID"] as? String ?: return@items

                                val participants = chat["participants"] as? List<*> ?: return@items

                                val chatType = chat["type"] as? String
                                    ?: if (participants.size > 2) "group" else "direct"

                                val unreadCounts =
                                    chat["unreadCounts"] as? Map<String, Long> ?: emptyMap()

                                val unreadCount =
                                    unreadCounts[currentUserID]?.toInt() ?: 0

                                if (chatType != "direct") {
                                    return@items
                                } else {

                                    val otherUserID = participants
                                        .mapNotNull { it as? String }
                                        .firstOrNull { it != currentUserID } ?: return@items

                                    if (otherUserID in hideBlockedUsers) return@items

                                    val user = userCache.value[otherUserID] ?: User(
                                        uid = otherUserID,
                                        name = "Loading Chat...",
                                        photoUrl = null
                                    )

                                    val lastMessage = chat["lastMessage"] as? String ?: ""
                                    val timestamp = chat["lastTimestamp"] as? Timestamp

                                    val previewText = if (lastMessage.isBlank()) {
                                        "Start conversation"
                                    } else {
                                        lastMessage
                                    }

                                    UserListItem(
                                        user = user,
                                        chatID = chatID,
                                        mutedChats = mutedChats,
                                        unreadCount = unreadCount,
                                        subtitle = previewText,
                                        timeText = formatTimestamp(timestamp),
                                        trailingContent = {
                                            var showMenu by remember { mutableStateOf(false) }
                                            Box {
                                                IconButton(
                                                    onClick = { showMenu = !showMenu }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.MoreVert,
                                                        contentDescription = "Options",
                                                        tint = MaterialTheme.colorScheme.onTertiary
                                                    )
                                                }

                                                DropdownMenu(
                                                    expanded = showMenu,
                                                    onDismissRequest = { showMenu = false },
                                                    containerColor = MaterialTheme.colorScheme.surface
                                                ) {
                                                    DropdownMenuItem(
                                                        text = {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Icon(
                                                                    imageVector = Icons.Default.PersonAdd,
                                                                    contentDescription = "Add Friend",
                                                                    tint = MaterialTheme.colorScheme.onTertiary
                                                                )
                                                                Spacer(modifier = Modifier.width(8.dp))
                                                                Text(
                                                                    "Add Friend",
                                                                    color = MaterialTheme.colorScheme.onTertiary
                                                                )
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
                                                                    tint = MaterialTheme.colorScheme.primary
                                                                )
                                                                Spacer(modifier = Modifier.width(8.dp))
                                                                Text(
                                                                    "Block User",
                                                                    color = MaterialTheme.colorScheme.primary
                                                                )
                                                            }
                                                        },
                                                        onClick = {
                                                            showMenu = false
                                                            userToBlock = otherUserID to user.name
                                                        }
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            navController.navigate(Routes.MESSAGES + "/${user.uid}")
                                        },
                                        onLongPress = {
                                            selectedChatID = chatID
                                            showActionSheet = true
                                        }
                                    )
                                }
                            }
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

                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (alphabetizedChats.isEmpty()) {
                        EmptyMessagesState(
                            title = "No message requests",
                            subtitle = "New message requests will show up here.",
                            icon = Icons.Default.Inbox
                        )
                    } else {
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
                                    .firstOrNull { it != currentUserID } ?: return@items

                                val user = userCache.value[otherUserID] ?: User(
                                    uid = otherUserID,
                                    name = "Loading Request...",
                                    photoUrl = null
                                )

                                UserListItem(
                                    user = user,
                                    chatID = chatID,
                                    mutedChats = mutedChats,
                                    unreadCount = 0,
                                    trailingContent = {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {

                                            IconButton(
                                                onClick = {
                                                    chatRepositoryViewModel.approveRequest(
                                                        chatID,
                                                        currentUserID,
                                                        otherUserID
                                                    )
                                                }
                                            ) {

                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Accept Request",
                                                    tint = MaterialTheme.colorScheme.onTertiary
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    chatRepositoryViewModel.denyRequest(
                                                        chatID
                                                    )
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Decline Request",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
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

    if (showActionSheet && selectedChatID != null) {
        val isMuted = selectedChatID != null && selectedChatID in mutedChats

        ModalBottomSheet(
            onDismissRequest = {
                showActionSheet = false
                selectedChatID = null
            }
        ) {
            Text(
                text = "Chat Actions",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Delete Chat") },
                leadingContent = { Icon(Icons.Default.Delete, contentDescription = null) },
                modifier = Modifier.clickable {
                    chatRepositoryViewModel.onDeleteChat(
                        userID = currentUserID,
                        chatID = selectedChatID!!
                    )
                    showActionSheet = false
                    selectedChatID = null
                }
            )

            ListItem(
                headlineContent = {
                    Text(if (isMuted) "Unmute Chat" else "Mute Chat")
                },
                leadingContent = {
                    if (isMuted) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null)
                    } else {
                        Icon(Icons.Default.VolumeOff, contentDescription = null)
                    }
                },
                modifier = Modifier.clickable {
                    if (isMuted) {
                        chatRepositoryViewModel.onUnmuteChat(
                            chatID = selectedChatID!!
                        )
                    } else {
                        chatRepositoryViewModel.onMuteChat(
                            chatID = selectedChatID!!
                        )
                    }
                    showActionSheet = false
                    selectedChatID = null
                }
            )

            Spacer(Modifier.height(16.dp))
        }
    }

    if (showCreateFriendGroupDialog) {
        CreateFriendGroupDialog(
            friends = friendList,
            currentUserID = currentUserID,
            onDismiss = {
                showCreateFriendGroupDialog = false
            },
            onCreate = { groupName, selectedFriendIDs ->
                chatRepositoryViewModel.callCreateGroupChat(
                    creatorUserID = currentUserID,
                    participantIDs = selectedFriendIDs,
                    groupName = groupName,
                    onCreated = { chatID ->
                        showCreateFriendGroupDialog = false
                        navController.navigate(
                            Routes.groupMessagesRoute(
                                chatID = chatID,
                                groupName = groupName
                            )
                        )
                    }
                )
            }
        )
    }
}

@Composable
fun CreateFriendGroupDialog(
    friends: List<User>,
    currentUserID: String,
    onDismiss: () -> Unit,
    onCreate: (String, List<String>) -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    var selectedFriendIDs by remember { mutableStateOf(setOf<String>()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create group chat") },
        text = {
            Column {
                TextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Group name") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Select friends")

                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    items(friends) { friend ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedFriendIDs =
                                        if (friend.uid in selectedFriendIDs) {
                                            selectedFriendIDs - friend.uid
                                        } else {
                                            selectedFriendIDs + friend.uid
                                        }
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = friend.uid in selectedFriendIDs,
                                onCheckedChange = { checked ->
                                    selectedFriendIDs =
                                        if (checked) {
                                            selectedFriendIDs + friend.uid
                                        } else {
                                            selectedFriendIDs - friend.uid
                                        }
                                }
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(friend.name)
                        }
                    }
                }

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        groupName.isBlank() -> {
                            errorMessage = "Enter a group name"
                        }

                        selectedFriendIDs.isEmpty() || selectedFriendIDs.size < 2 -> {
                            errorMessage = "Select at least two friends"
                        }

                        else -> {
                            onCreate(groupName.trim(), selectedFriendIDs.toList())
                        }
                    }
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun UserListItem(
    user: User,
    chatID: String,
    mutedChats: Set<String>,
    unreadCount: Int,
    subtitle: String = "",
    timeText: String? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = ripple(),
                    onClick = { onClick?.invoke() },
                    onLongClick = { onLongPress?.invoke() }
                )
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                name = user.name,
                photoUrl = user.photoUrl,
                size = 44
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (unreadCount > 0) {
                            user.name + "  ($unreadCount)"
                        }
                        else {
                            user.name
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onTertiary
                        ),
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    if (timeText != null) {
                        Text(
                            text = timeText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (subtitle.isNotBlank()) {
                        Text(
                            text = subtitle,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (mutedChats.contains(chatID)) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.VolumeOff,
                            "",
                            tint = MaterialTheme.colorScheme.onTertiary
                        )
                    }
                }

            }



            if (trailingContent != null) {
                Spacer(modifier = Modifier.width(4.dp))
                trailingContent()
            }
        }

        HorizontalDivider(
            color = Color(0xFFEAEAEA),
            thickness = 0.8.dp,
            modifier = Modifier.padding(start = 64.dp, end = 8.dp)
        )
    }
}
fun formatTimestamp(timestamp: Timestamp?): String {
    if (timestamp == null) return ""

    val date = timestamp.toDate()
    val now = Calendar.getInstance()
    val msgTime = Calendar.getInstance().apply { time = date }

    return if (
        now.get(Calendar.DAY_OF_YEAR) == msgTime.get(Calendar.DAY_OF_YEAR) &&
        now.get(Calendar.YEAR) == msgTime.get(Calendar.YEAR)
    ) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
    } else {
        SimpleDateFormat("MMM d", Locale.getDefault()).format(date)
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
            val filteredParticipants = chat["participants"] as? List<*> ?: emptyList<Any>()

            val chatType = chat["type"] as? String
                ?: if (filteredParticipants.size > 2) "group" else "direct"

            val sortBasedOnChatType = when (chatType) {
                "group" -> chat["groupName"] as? String ?: "group chat"

                else -> {

                    val filteredOtherUserID = filteredParticipants
                        .mapNotNull { it as? String }
                        .firstOrNull { it != currentUserID }

                    userCache.value[filteredOtherUserID]?.name ?: "Unknown User"
                }
            }
            sortBasedOnChatType.lowercase()
        }.thenBy { chat ->
            chat["chatID"] as? String ?: ""
        }
    )

    return sortedChats
}

@Composable
fun EmptyMessagesState(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}