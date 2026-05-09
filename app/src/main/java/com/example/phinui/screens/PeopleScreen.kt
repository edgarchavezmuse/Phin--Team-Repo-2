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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.phinui.components.people.AlreadyFriendDialog
import com.example.phinui.components.people.BlockUserDialog
import com.example.phinui.components.people.SendFriendRequestDialog
import com.example.phinui.components.people.UnblockUserDialog
import com.example.phinui.data.friends.FriendRepository
import com.example.phinui.ui.components.UserAvatar
import com.example.phinui.ui.theme.Background
import com.example.phinui.ui.theme.HeaderRed
import com.example.phinui.ui.theme.NavText
import com.example.phinui.ui.theme.TextMuted
import com.example.phinui.viewmodel.ChatRepositoryViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.example.phinui.components.people.ActiveChatDialog
import androidx.compose.material3.*
import com.example.phinui.components.people.PendingFriendRequestDialog
import androidx.compose.foundation.clickable
import androidx.navigation.NavHostController
import com.example.phinui.ui.components.UserProfilePreviewDialog
import com.example.phinui.data.model.PreviewUser
import com.example.phinui.ui.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleScreen(
    navController: NavHostController
) {
    val repo = remember { FriendRepository() }
    val db = remember { FirebaseFirestore.getInstance() }
    val auth = remember { FirebaseAuth.getInstance() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val chatRepositoryViewModel: ChatRepositoryViewModel = viewModel()

    var selectedTab by remember { mutableIntStateOf(0) }

    var search by remember { mutableStateOf("") }
    var majorFilter by remember { mutableStateOf("All Majors") }
    var majorMenuExpanded by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList()) }
    var blockedUsers by remember { mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList()) }
    var blockedByOthers by remember { mutableStateOf<Set<String>>(emptySet()) }
    var message by remember { mutableStateOf<String?>(null) }
    var myName by remember { mutableStateOf("") }
    var friends by remember { mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList()) }
    var alreadyFriendUser by remember { mutableStateOf<String?>(null) }
    var allUsers by remember { mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList()) }
    var pendingRequestUser by remember { mutableStateOf<String?>(null) }
    var previewUser by remember { mutableStateOf<PreviewUser?>(null) }
    var mutualFriendsCount by remember { mutableIntStateOf(0) }

    var userToAdd by remember { mutableStateOf<Pair<String, String>?>(null) }
    var userToBlock by remember { mutableStateOf<Pair<String, String>?>(null) }
    var userToUnblock by remember { mutableStateOf<Pair<String, String>?>(null) }

    val chats = chatRepositoryViewModel.approvedChats.value

    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid ?: return@LaunchedEffect
        db.collection("users").document(uid).get()
            .addOnSuccessListener {
                myName = it.getString("name") ?: ""
            }
    }

    DisposableEffect(Unit) {
        val usersListener = db.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    message = error.message
                    return@addSnapshotListener
                }

                val currentUid = auth.currentUser?.uid

                allUsers = snapshot?.documents
                    ?.mapNotNull { doc ->
                        val uid = doc.id
                        val data = doc.data

                        if (uid != currentUid && data != null) {
                            uid to data
                        } else null
                    }
                    ?.sortedBy { (_, data) ->
                        (data["name"] as? String)?.trim()?.lowercase() ?: ""
                    }
                    ?: emptyList()
            }

        onDispose {
            usersListener.remove()
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

        val myUid = auth.currentUser?.uid
        val blockedByOthersListener =
            if (myUid != null) {
                db.collection("users")
                    .whereArrayContains("blocked", myUid)
                    .addSnapshotListener { snapshot, error ->
                        if (error == null && snapshot != null) {
                            blockedByOthers = snapshot.documents.map { it.id }.toSet()
                        }
                    }
            } else {
                null
            }

        onDispose {
            blockedListener?.remove()
            friendsListener?.remove()
            blockedByOthersListener?.remove()
        }
    }

    DisposableEffect(search) {
        val searchListener =
            repo.listenSearchUsersByNamePrefix(
                query = search,
                onResult = {
                    searchResults = it.sortedBy { (_, data) ->
                        (data["name"] as? String)?.trim()?.lowercase() ?: ""
                    }
                },
                onError = { message = it.message }
            )

        onDispose {
            searchListener?.remove()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "People",
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onTertiary
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
                val majors = listOf("All Majors") +
                        allUsers.mapNotNull { (_, user) ->
                            (user["major"] as? String)?.trim()
                        }
                            .filter { it.isNotBlank() }
                            .map { it.lowercase() }            // normalize
                            .distinct()                        // remove duplicates
                            .map { it.split(" ").joinToString(" ") { word ->
                                word.replaceFirstChar { c -> c.uppercase() }  // Title Case
                            }}
                            .sorted()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it },
                        label = { Text("Search users") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = { keyboardController?.hide() }
                        )
                    )

                    Box {
                        IconButton(
                            onClick = { majorMenuExpanded = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Filter by major",
                                tint = MaterialTheme.colorScheme.onTertiary
                            )
                        }

                        DropdownMenu(
                            expanded = majorMenuExpanded,
                            onDismissRequest = { majorMenuExpanded = false },
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            majors.forEach { major ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = major,
                                            color = MaterialTheme.colorScheme.onTertiary
                                        )
                                    },
                                    onClick = {
                                        majorFilter = major
                                        majorMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (majorFilter != "All Majors") {
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Major: $majorFilter",
                        color = TextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val usersToDisplay =
                    if (search.isBlank()) allUsers else searchResults

                val visibleUsers = usersToDisplay.filter { (uid, user) ->
                    val major = user["major"] as? String ?: ""

                    blockedUsers.none { (blockedUid, _) -> blockedUid == uid } &&
                            uid !in blockedByOthers &&
                            (majorFilter == "All Majors" || major.equals(majorFilter, ignoreCase = true))
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(visibleUsers) { (uid, user) ->
                        val name = user["name"] as? String ?: "Unknown"
                        val email = user["email"] as? String ?: ""
                        val photoUrl = user["photoUrl"] as? String

                        val currentUserId = chatRepositoryViewModel.currentUserID

                        val chatForUser = chats.firstOrNull { chat ->
                            val participants = chat["participants"] as? List<*> ?: return@firstOrNull false
                            val ids = participants.mapNotNull { it as? String }

                            currentUserId != null && currentUserId in ids && uid in ids
                        }

                        val requestState = chatForUser?.get("requestState") as? String
                        val showSendMessage = requestState == "approved"
                        val label = if (showSendMessage) "Send Message" else "Send Message Request"

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
                                UserAvatar(
                                    name = name,
                                    photoUrl = photoUrl,
                                    size = 44,
                                    modifier = Modifier.clickable {

                                        mutualFriendsCount = 0

                                        previewUser = PreviewUser(
                                            name = name,
                                            email = email,
                                            photoUrl = photoUrl,
                                            major = user["major"] as? String ?: "",
                                            bio = user["bio"] as? String ?: ""
                                        )

                                        val myFriendIds = friends.map { it.first }.toSet()

                                        db.collection("users")
                                            .document(uid)
                                            .collection("friends")
                                            .get()
                                            .addOnSuccessListener { snapshot ->

                                                val otherFriendIds =
                                                    snapshot.documents.map { it.id }.toSet()

                                                mutualFriendsCount =
                                                    myFriendIds.intersect(otherFriendIds).size
                                            }
                                            .addOnFailureListener {
                                                mutualFriendsCount = 0
                                            }
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))

                                Column(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = name,
                                        color = MaterialTheme.colorScheme.onTertiary
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
                                                Text("Add Friend")
                                            }
                                        },
                                        onClick = {
                                            showMenu = false

                                            val isFriend = friends.any { (friendUid, _) ->
                                                friendUid == uid
                                            }

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
                                                    contentDescription = label,
                                                    tint = MaterialTheme.colorScheme.onTertiary
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(label, color = MaterialTheme.colorScheme.onTertiary)
                                            }
                                        },
                                        onClick = {
                                            val senderID = chatRepositoryViewModel.currentUserID
                                                ?: return@DropdownMenuItem

                                            if (showSendMessage) {
                                                navController.navigate(Routes.MESSAGES + "/$uid")
                                            } else {
                                                chatRepositoryViewModel.sendMessageRequest(
                                                    senderID,
                                                    uid,
                                                    name
                                                )
                                            }
                                            showMenu = false
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
                                                Text("Block User", color = MaterialTheme.colorScheme.primary)
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
                        val photoUrl = user["photoUrl"] as? String

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
                                UserAvatar(
                                    name = name,
                                    photoUrl = photoUrl,
                                    size = 44,
                                    modifier = Modifier.clickable {

                                        mutualFriendsCount = 0

                                        previewUser = PreviewUser(
                                            name = name,
                                            email = email,
                                            photoUrl = photoUrl,
                                            major = user["major"] as? String ?: "",
                                            bio = user["bio"] as? String ?: ""
                                        )

                                        val myFriendIds = friends.map { it.first }.toSet()

                                        db.collection("users")
                                            .document(uid)
                                            .collection("friends")
                                            .get()
                                            .addOnSuccessListener { snapshot ->

                                                val otherFriendIds =
                                                    snapshot.documents.map { it.id }.toSet()

                                                mutualFriendsCount =
                                                    myFriendIds.intersect(otherFriendIds).size
                                            }
                                            .addOnFailureListener {
                                                mutualFriendsCount = 0
                                            }
                                    }
                                )
                                Spacer(modifier = Modifier.width(12.dp))

                                Column(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = name,
                                        color = MaterialTheme.colorScheme.onTertiary
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
                                    tint = MaterialTheme.colorScheme.primary
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
                        if (it.message == "PENDING_REQUEST_EXISTS") {
                            pendingRequestUser = name
                        } else {
                            message = it.message
                        }
                        userToAdd = null
                    }
                )
            },
            onDismiss = { userToAdd = null }
        )
    }

    pendingRequestUser?.let { name ->
        PendingFriendRequestDialog(
            name = name,
            onDismiss = { pendingRequestUser = null }
        )
    }

    alreadyFriendUser?.let { name ->
        AlreadyFriendDialog(
            name = name,
            onDismiss = { alreadyFriendUser = null }
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

    previewUser?.let { user ->
        UserProfilePreviewDialog(
            mutualFriendsCount = mutualFriendsCount,
            name = user.name,
            email = user.email,
            photoUrl = user.photoUrl,
            major = user.major,
            bio = user.bio,
            onDismiss = { previewUser = null }
        )
    }

    val receiverUserName = chatRepositoryViewModel.activeChatUserName.value ?: "Unknown"
    if (chatRepositoryViewModel.showMessageApprovedDialog.value) {
        ActiveChatDialog(
            name = receiverUserName,
            onDismiss = {
                chatRepositoryViewModel.showMessageApprovedDialog.value = false
            }
        )
    }

    chatRepositoryViewModel.confirmSendMessageRequest.value?.let { confirmSendMessageRequest ->
        Toast.makeText(
            context,
            confirmSendMessageRequest,
            Toast.LENGTH_SHORT
        ).show()
        chatRepositoryViewModel.confirmSendMessageRequest.value = null
    }
}