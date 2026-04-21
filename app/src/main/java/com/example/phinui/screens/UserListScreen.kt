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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.lifecycle.viewModelScope
import com.example.phinui.components.messages.ChatRepository
import com.example.phinui.components.people.BlockFriendDialog
import com.example.phinui.components.people.BlockUserDialog
import com.example.phinui.components.people.RemoveFriendDialog
import com.example.phinui.components.people.SendFriendRequestDialog
import com.example.phinui.data.friends.FriendRepository
import com.example.phinui.viewmodel.ChatRepositoryViewModel
import com.example.phinui.viewmodel.FriendRepositoryViewModel
import com.example.phinui.viewmodel.FriendRepositoryViewModelFactory
import com.example.phinui.viewmodel.UserListViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope


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
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(friendList) { friend ->
                            if(friend.uid in hideBlockedUsers) return@items
                            UserListItem(
                                user = friend,
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
                                                    friendToRemove = friend.uid to friend.name
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
                                                    friendToBlock = friend.uid to friend.name
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
                                    navController.navigate(Routes.MESSAGES + "/${friend.uid}")
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
                val userNameCache = remember { mutableStateOf<Map<String, String>>(emptyMap()) }

                val alphabetizedChats = getSortedChats(
                    approvedChatsState = approvedChatsState,
                    chatRepositoryViewModel = chatRepositoryViewModel,
                    userNameCache = userNameCache,
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
                            val participants = chat["participants"] as? List<*> ?: return@items

                            val otherUserID = participants
                                .mapNotNull { it as? String }
                                .firstOrNull{ it != currentUserID } ?:return@items

                            if(otherUserID in hideBlockedUsers) return@items

                            val userName = userNameCache.value[otherUserID] ?: "Loading Chat..."

                            val user = User(
                                uid = otherUserID,
                                name = userName
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
                                                    userToAdd = otherUserID to userName
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
                                                    userToBlock = otherUserID to userName
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

                val userNameCache = remember { mutableStateOf<Map<String, String>>(emptyMap()) }

                val alphabetizedChats = getSortedChats(
                    approvedChatsState = messageRequestState,
                    chatRepositoryViewModel = chatRepositoryViewModel,
                    userNameCache = userNameCache,
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

                            val userName = userNameCache.value[otherUserID] ?: "Loading Request..."

                            val user = User(
                                uid = otherUserID,
                                name = userName
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

@Composable fun getSortedChats(
    approvedChatsState: List<Map<String, Any>>,
    chatRepositoryViewModel: ChatRepositoryViewModel,
    userNameCache: MutableState<Map<String, String>>,
    currentUserID: String
): List<Map<String,Any>> {
    LaunchedEffect(approvedChatsState) {
        val allUserIds = approvedChatsState.flatMap { chat ->
            val participants = chat["participants"] as? List<*> ?: return@flatMap emptyList<String>()
            participants.mapNotNull { it as? String }
        }.distinct()

        val userNamesMap = mutableMapOf<String, String>()
        val deferredResults = allUserIds.map { otherUserID ->
            async{
                try{
                    val userName = chatRepositoryViewModel.userListRepository.getUserNameByID(otherUserID)
                    userNamesMap[otherUserID] = userName

                } catch (e: Exception) {
                    userNamesMap[otherUserID] = "Unknown User"
                }
            }
        }
        coroutineScope {
            deferredResults.awaitAll()
        }
        userNameCache.value = userNamesMap
    }

    val sortedChats = approvedChatsState.sortedWith (compareBy<Map<String, Any>> { chat ->
        val filteredParticipants = chat["participants"] as? List<*> ?: return@compareBy ""
        val filteredOtherUserID = filteredParticipants
            .mapNotNull { it as? String }
            .firstOrNull{ it != currentUserID }
        val userName = userNameCache.value[filteredOtherUserID] ?: "Unknown User"
        userName.lowercase()
    }.thenBy { chat ->
        chat["chatID"] as? String ?: ""
    })
    return sortedChats
}