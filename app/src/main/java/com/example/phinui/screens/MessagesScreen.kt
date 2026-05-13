package com.example.phinui.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.phinui.components.messages.UserState
import com.example.phinui.data.CampusLocation
import com.example.phinui.data.fetchCampusLocations
import com.example.phinui.data.calendar.CalendarEvent
import com.example.phinui.data.calendar.CalendarSource
import com.example.phinui.notifications.ReminderScheduler
import com.example.phinui.ui.navigation.Routes
import com.example.phinui.ui.theme.DeletedMessageColor
import com.example.phinui.viewmodel.CalendarViewModel
import com.example.phinui.viewmodel.CalendarViewModelFactory
import com.example.phinui.viewmodel.ChatRepositoryViewModel
import com.example.phinui.viewmodel.UserListViewModel
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.delay
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.TimePickerDefaults
import com.example.phinui.ui.components.UserAvatar

@Composable
fun MessagesScreen(
    senderUserID: String,
    receiverUserID: String,
    navController: NavController,
    userListViewModel: UserListViewModel = viewModel(),
    chatID: String? = null,
    groupName: String? = null,
    isGroupChat: Boolean = false,
    setTopBarTitle: (String, Boolean) -> Unit
) {
    val chatRepositoryViewModel: ChatRepositoryViewModel = viewModel()
    val context = LocalContext.current
    val reminderScheduler = remember { ReminderScheduler(context) }
    val calendarViewModel: CalendarViewModel = viewModel(
        factory = CalendarViewModelFactory(
            context = context.applicationContext,
            reminderScheduler = reminderScheduler
        )
    )

    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf<Map<String, Any>>()) }

    var showPinPickerDialog by remember { mutableStateOf(false) }
    var campusPins by remember { mutableStateOf<List<CampusLocation>>(emptyList()) }
    var showAttachMenu by remember { mutableStateOf(false) }

    val autoScrollState = rememberLazyListState()
    val autoScrollThreshold = 3
    var initialChatOpen by remember { mutableStateOf(true) }

    val selectedMessage = remember { mutableStateOf<Map<String, Any>?>(null) }
    val showDeleteDialog = remember { mutableStateOf(false) }
    val resolvedChatID = chatID ?: chatRepositoryViewModel.callGetChatID(senderUserID, receiverUserID)

    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    var selectedStudyDate by remember { mutableStateOf("") }
    var selectedStudyStartTime by remember { mutableStateOf<LocalTime?>(null) }
    var selectedStudyEndTime by remember { mutableStateOf<LocalTime?>(null) }

    var showStudySessionInviteDialog by remember { mutableStateOf(false) }

    val state = userListViewModel.userState

    var visibleTimestampMessageIds by remember { mutableStateOf(setOf<String>()) }
    var shouldScrollLatestTimestamp by remember { mutableStateOf(false) }

    var userNameCache by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var userPhotoCache by remember { mutableStateOf<Map<String, String?>>(emptyMap()) }

    LaunchedEffect(messages) {
        val senderIDs = messages.mapNotNull { it["senderID"] as? String }.distinct()

        val newNames = mutableMapOf<String, String>()
        val newPhotos = mutableMapOf<String, String?>()

        senderIDs.forEach { id ->
            if (!userNameCache.containsKey(id)) {
                try {
                    val name = chatRepositoryViewModel.userListRepository.getUserNameByID(id)
                    val photoUrl = chatRepositoryViewModel.userListRepository.getUserPhotoUrlByID(id)

                    newNames[id] = name
                    newPhotos[id] = photoUrl
                } catch (_: Exception) {
                    newNames[id] = "Unknown"
                }
            }
        }

        userNameCache = userNameCache + newNames
        userPhotoCache = userPhotoCache + newPhotos
    }

    LaunchedEffect(isGroupChat, groupName) {
        if (isGroupChat) {
            setTopBarTitle(groupName ?: "Group Chat", true)
        }
    }

    LaunchedEffect(state, isGroupChat) {
        if (!isGroupChat) {
            when (state) {
                is UserState.Loading -> setTopBarTitle("Loading...", true)
                is UserState.Loaded -> setTopBarTitle("Chatting with ${state.user.name}", true)
                else -> {}
            }
        }
    }

    LaunchedEffect(receiverUserID, isGroupChat) {
        if (!isGroupChat) {
            userListViewModel.loadSelectedUser(receiverUserID)
        }
    }

    LaunchedEffect(senderUserID, receiverUserID, resolvedChatID, isGroupChat) {
        if (isGroupChat) {
            chatRepositoryViewModel.callCheckForMessagesByChatID(resolvedChatID) { newMessages ->
                messages = sortMessagesByTime(newMessages)
            }
        } else {
            chatRepositoryViewModel.callCheckForNewMessage(senderUserID, receiverUserID) { newMessages ->
                messages = sortMessagesByTime(newMessages)
            }
        }
    }

    LaunchedEffect(Unit) {
        campusPins = fetchCampusLocations()
    }

    LaunchedEffect(resolvedChatID) {
        chatRepositoryViewModel.onChatOpened(senderUserID, resolvedChatID)
    }

    DisposableEffect(resolvedChatID) {
        onDispose {
            chatRepositoryViewModel.onChatClosed(senderUserID)
        }
    }

    LaunchedEffect(messages.size) {
        chatRepositoryViewModel.markChatsAsRead(senderUserID, resolvedChatID)

        if (messages.isEmpty()) return@LaunchedEffect

        val lastMessageIndex = messages.lastIndex

        val lastVisibleIndex = autoScrollState.layoutInfo.visibleItemsInfo
            .lastOrNull()
            ?.index

        val userIsNearBottom = lastVisibleIndex == null ||
                lastMessageIndex - lastVisibleIndex <= autoScrollThreshold

        val newestMessageIsMine =
            (messages.lastOrNull()?.get("senderID") as? String) == senderUserID

        if (initialChatOpen || userIsNearBottom || newestMessageIsMine) {
            delay(50)
            autoScrollState.scrollToItem(lastMessageIndex)
            initialChatOpen = false
        }
    }

    var groupInitialScrollFixDone by remember(resolvedChatID) { mutableStateOf(false) }

    LaunchedEffect(userNameCache.size, userPhotoCache.size, messages.size, isGroupChat) {
        if (
            isGroupChat &&
            messages.isNotEmpty() &&
            !groupInitialScrollFixDone
        ) {
            delay(250)
            autoScrollState.scrollToItem(messages.lastIndex)
            groupInitialScrollFixDone = true
        }
    }

    LaunchedEffect(shouldScrollLatestTimestamp) {
        if (shouldScrollLatestTimestamp && messages.isNotEmpty()) {
            autoScrollState.scrollToItem(messages.lastIndex)
            shouldScrollLatestTimestamp = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showDeleteDialog.value && selectedMessage.value != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog.value = false
                    selectedMessage.value = null
                },
                title = { Text("Delete message?") },
                text = { Text("This message will be deleted for everyone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val message = selectedMessage.value ?: return@TextButton
                            chatRepositoryViewModel.onDeleteMessage(
                                message = message,
                                chatID = resolvedChatID
                            )
                            showDeleteDialog.value = false
                            selectedMessage.value = null
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog.value = false
                            selectedMessage.value = null
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            reverseLayout = false,
            state = autoScrollState,
            verticalArrangement = Arrangement.Top,
        ) {
            items(
                items = messages,
                key = { message ->
                    message["messageID"] as String
                }
            ) { message ->
                val type = message["type"] as? String ?: "text"
                val text = message["text"] as? String ?: ""

                val senderID = message["senderID"] as? String ?: ""
                val senderName = userNameCache[senderID]
                val senderPhotoUrl = userPhotoCache[senderID]
                val isDeleted = message["deleted"] as? Boolean ?: false
                val messageID = message["messageID"] as? String ?: ""
                val messageChatID = message["chatID"] as? String ?: ""
                val startTime = message["startTime"] as? Timestamp
                val endTime = message["endTime"] as? Timestamp
                val isMyMessage = senderID == senderUserID

                val studySessionTitle = message["title"] as? String ?: "Study Session"
                val studySessionDescription = message["description"] as? String ?: ""
                val participants = message["participants"] as? Map<String, String> ?: emptyMap()
                val myStatus = participants[senderUserID] ?: "PENDING"

                val convertDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val convertTime = SimpleDateFormat("h:mm a", Locale.getDefault())
                val displayDate = startTime?.toDate()?.let { convertDate.format(it) } ?: ""
                val displayStartTime = startTime?.toDate()?.let { convertTime.format(it) } ?: ""
                val displayEndTime = endTime?.toDate()?.let { convertTime.format(it) } ?: ""

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    horizontalArrangement = if (isMyMessage) Arrangement.End else Arrangement.Start
                ) {
                    MessageBubble(
                        message = message,
                        isMyMessage = isMyMessage,
                        isDeleted = isDeleted,
                        senderUserID = senderUserID,
                        receiverUserID = receiverUserID,
                        navController = navController,
                        chatRepositoryViewModel = chatRepositoryViewModel,
                        calendarViewModel = calendarViewModel,
                        chatID = resolvedChatID,
                        showTimestamp = messageID in visibleTimestampMessageIds,
                        onTapMessage = {
                            visibleTimestampMessageIds = visibleTimestampMessageIds + messageID

                            val latestMessageId = messages.lastOrNull()?.get("messageID") as? String
                            shouldScrollLatestTimestamp = (messageID == latestMessageId)
                        },
                        onHideTimestamp = {
                            visibleTimestampMessageIds = visibleTimestampMessageIds - messageID
                        },
                        senderName = if (isGroupChat) senderName ?: "" else null,
                        senderPhotoUrl = if (isGroupChat) senderPhotoUrl else null,
                        onLongPressMine = {
                            selectedMessage.value = message
                            showDeleteDialog.value = true
                        }
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(32.dp)
                    )
                    .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(start = 12.dp, end = 4.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onTertiary
                            ),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                if (messageText.isEmpty()) {
                                    Text(
                                        text = "Type a message...",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 15.sp
                                    )
                                }
                                innerTextField()
                            }
                        )

                        Button(
                            onClick = {
                                if (messageText.isNotBlank()) {
                                    if (isGroupChat) {
                                        chatRepositoryViewModel.callSendGroupMessage(
                                            senderUserID,
                                            resolvedChatID,
                                            messageText
                                        )
                                    } else {
                                        chatRepositoryViewModel.callSendMessage(
                                            senderUserID,
                                            receiverUserID,
                                            messageText
                                        )
                                    }
                                    messageText = ""
                                }
                            },
                            modifier = Modifier.height(40.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onTertiary
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            Text("Send", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                Box {
                    IconButton(onClick = { showAttachMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }

                    DropdownMenu(
                        expanded = showAttachMenu,
                        onDismissRequest = { showAttachMenu = false },
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        DropdownMenuItem(
                            text = { Text("Study session invite") },
                            onClick = {
                                showAttachMenu = false
                                showStudySessionInviteDialog = true
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Share campus pin") },
                            onClick = {
                                showAttachMenu = false
                                showPinPickerDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

        if (showStudySessionInviteDialog) {
            var studySessionTitle by remember { mutableStateOf("") }
            var studySessionDescription by remember { mutableStateOf("") }
            val amPMTime = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
            var emptyFieldChecker by remember { mutableStateOf<String?>(null) }

            AlertDialog(
                onDismissRequest = { showStudySessionInviteDialog = false },
                containerColor = MaterialTheme.colorScheme.surface,
                title = { Text("Create study session", color = MaterialTheme.colorScheme.onTertiary) },
                text = {
                    Column {
                        TextField(
                            value = studySessionTitle,
                            onValueChange = { studySessionTitle = it },
                            label = { Text("Title") }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        TextField(
                            value = studySessionDescription,
                            onValueChange = { studySessionDescription = it },
                            label = { Text("Description") }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDatePicker = true }
                                .padding(12.dp)
                        ) {
                            Icon(Icons.Default.DateRange, contentDescription = "Date")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (selectedStudyDate.isNotEmpty()) {
                                    selectedStudyDate
                                } else {
                                    "Select date"
                                }
                            )
                        }

                        if (showDatePicker) {
                            EventDatePickerDialog(
                                initialDate = selectedStudyDate,
                                onDismiss = { showDatePicker = false },
                                onConfirm = { date ->
                                    selectedStudyDate = date
                                    showDatePicker = false
                                }
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showStartTimePicker = true }
                                .padding(12.dp)
                        ) {
                            Icon(Icons.Default.AccessTime, contentDescription = "Start time")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = selectedStudyStartTime?.format(amPMTime) ?: "Select start time"
                            )
                        }

                        if (showStartTimePicker) {
                            EventTimePickerDialog(
                                title = "Start Time",
                                initialTime = selectedStudyStartTime,
                                onDismiss = { showStartTimePicker = false },
                                onConfirm = { selectedTime ->
                                    selectedStudyStartTime = selectedTime
                                    showStartTimePicker = false
                                }
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showEndTimePicker = true }
                                .padding(12.dp)
                        ) {
                            Icon(Icons.Default.AlarmOff, contentDescription = "End time")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = selectedStudyEndTime?.format(amPMTime) ?: "Select end time"
                            )
                        }

                        if (showEndTimePicker) {
                            EventTimePickerDialog(
                                title = "End Time",
                                initialTime = selectedStudyEndTime,
                                onDismiss = { showEndTimePicker = false },
                                onConfirm = { selectedTime ->
                                    selectedStudyEndTime = selectedTime
                                    showEndTimePicker = false
                                }
                            )
                        }

                        emptyFieldChecker?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val eventStartTime = selectedStudyStartTime
                            val eventEndTime = selectedStudyEndTime

                            when {
                                studySessionTitle.isBlank() -> {
                                    emptyFieldChecker = "Please enter a title"
                                    return@Button
                                }

                                selectedStudyDate.isBlank() -> {
                                    emptyFieldChecker = "Please select a date"
                                    return@Button
                                }

                                eventStartTime == null -> {
                                    emptyFieldChecker = "Please select a start time"
                                    return@Button
                                }

                                eventEndTime == null -> {
                                    emptyFieldChecker = "Please select an end time"
                                    return@Button
                                }

                                eventEndTime <= eventStartTime -> {
                                    emptyFieldChecker = "End time must be after start time"
                                    return@Button
                                }

                                else -> {
                                    emptyFieldChecker = null
                                }
                            }

                            val studyStartTime = convertToCalendarTimestamp(
                                selectedStudyDate,
                                selectedStudyStartTime!!
                            )

                            val studyEndTime = convertToCalendarTimestamp(
                                selectedStudyDate,
                                selectedStudyEndTime!!
                            )

                            if (isGroupChat) {
                                chatRepositoryViewModel.callSendGroupStudySessionInvitation(
                                    senderUserID = senderUserID,
                                    chatID = resolvedChatID,
                                    studySessionTitle = studySessionTitle,
                                    studySessionDescription = studySessionDescription,
                                    startTime = studyStartTime,
                                    endTime = studyEndTime
                                )
                            } else {
                                chatRepositoryViewModel.callSendStudySessionInvitation(
                                    senderUserID = senderUserID,
                                    receiverUserID = receiverUserID,
                                    studySessionTitle = studySessionTitle,
                                    studySessionDescription = studySessionDescription,
                                    startTime = studyStartTime,
                                    endTime = studyEndTime
                                )
                            }

                            val timeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                            val convertStudyStartTime = studyStartTime
                                .toDate()
                                .toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime()
                                .format(timeFormatter)

                            val convertStudyEndTime = studyEndTime
                                .toDate()
                                .toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime()
                                .format(timeFormatter)

                            val createStudySessionEvent = CalendarEvent(
                                id = "",
                                title = studySessionTitle,
                                description = studySessionDescription,
                                start = convertStudyStartTime,
                                end = convertStudyEndTime,
                                location = null,
                                reminderMinutes = emptyList(),
                                isAllDay = false,
                                colorHex = "#DC2127",
                                source = CalendarSource.LOCAL
                            )

                            calendarViewModel.saveStudySessionEvent(createStudySessionEvent) { success ->
                                if (success) {
                                    Toast.makeText(
                                        context,
                                        "$studySessionTitle added to your local calendar",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Failed to add $studySessionTitle to your local calendar",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            showStudySessionInviteDialog = false
                        }
                    ) {
                        Text("Send")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            showStudySessionInviteDialog = false
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showPinPickerDialog) {
            AlertDialog(
                onDismissRequest = { showPinPickerDialog = false },
                containerColor = MaterialTheme.colorScheme.surface,
                title = { Text("Share campus pin") },
                text = {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 400.dp)
                    ) {
                        items(campusPins) { pin ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isGroupChat) {
                                            chatRepositoryViewModel.callSendPingGroupMessage(
                                                senderUserID = senderUserID,
                                                chatID = resolvedChatID,
                                                location = pin
                                            )
                                        }
                                        else {
                                            chatRepositoryViewModel.callSendPinMessage(
                                                senderUserID = senderUserID,
                                                receiverUserID = receiverUserID,
                                                location = pin
                                            )
                                        }
                                        showPinPickerDialog = false
                                    }
                                    .padding(vertical = 12.dp)
                            ) {
                                Text(
                                    text = pin.name,
                                    fontWeight = FontWeight.Bold
                                )
                                if (pin.building.isNotBlank()) {
                                    Text(text = pin.building)
                                }
                                if (pin.category.isNotBlank()) {
                                    Text(text = pin.category)
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showPinPickerDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

private fun sortMessagesByTime(
    newMessages: List<Map<String, Any>>
): List<Map<String, Any>> {
    return newMessages.sortedWith(
        compareBy<Map<String, Any>> { message ->
            (message["timestamp"] as? Timestamp)?.seconds ?: Long.MAX_VALUE
        }.thenBy { message ->
            (message["timestamp"] as? Timestamp)?.nanoseconds ?: Int.MAX_VALUE
        }.thenBy { message ->
            message["messageID"] as? String ?: ""
        }
    )
}
fun convertToCalendarTimestamp(
    selectedStudyDate: String,
    selectedStudyTime: LocalTime
): Timestamp {
    val localDate = LocalDate.parse(selectedStudyDate)
    val dateTime = LocalDateTime.of(localDate, selectedStudyTime)
    val instant = dateTime.atZone(ZoneId.systemDefault()).toInstant()

    return Timestamp(instant.epochSecond, instant.nano)
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun EventDatePickerDialog(
    initialDate: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val accentRed = MaterialTheme.colorScheme.primary

    val initialMillis = remember(initialDate) {
        parseIsoDateToMillis(initialDate)
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        onConfirm(formatMillisToIsoDate(millis))
                    }
                },
                enabled = datePickerState.selectedDateMillis != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentRed,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = accentRed)
            }
        },
        colors = DatePickerDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        DatePicker(
            state = datePickerState,
            showModeToggle = true,
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onTertiary,
                headlineContentColor = MaterialTheme.colorScheme.onTertiary,
                weekdayContentColor = Color.DarkGray,
                subheadContentColor = Color.DarkGray,
                selectedDayContainerColor = accentRed,
                selectedDayContentColor = MaterialTheme.colorScheme.onTertiary,
                todayDateBorderColor = accentRed,
                todayContentColor = accentRed,
                selectedYearContainerColor = accentRed,
                selectedYearContentColor = MaterialTheme.colorScheme.onTertiary,
                dayContentColor = MaterialTheme.colorScheme.onSurface,
                disabledDayContentColor = Color.LightGray
            )
        )
    }
}

private fun parseIsoDateToMillis(date: String): Long? {
    return try {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        formatter.parse(date)?.time
    } catch (_: Exception) {
        null
    }
}

private fun formatMillisToIsoDate(millis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date(millis))
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun EventTimePickerDialog(
    title: String,
    initialTime: LocalTime?,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit
) {
    val initialHour24 = remember(initialTime) {
        initialTime?.hour ?: 9
    }

    val initialMinute = remember(initialTime) {
        initialTime?.minute ?: 0
    }

    val timePickerState = rememberTimePickerState(
        initialHour = initialHour24,
        initialMinute = initialMinute,
        is24Hour = false
    )

    var showDial by rememberSaveable { mutableStateOf(false) }

    val accentRed = MaterialTheme.colorScheme.primary
    val cardColor = MaterialTheme.colorScheme.surface
    val titleColor = MaterialTheme.colorScheme.onTertiary
    val bodyColor = MaterialTheme.colorScheme.onSurfaceVariant

    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = cardColor,
            shadowElevation = 10.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = titleColor,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(onClick = { showDial = !showDial }) {
                        Icon(
                            imageVector = if (showDial) Icons.Outlined.Edit else Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = accentRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (showDial) "Dial picker" else "Enter time",
                    style = MaterialTheme.typography.bodyMedium,
                    color = bodyColor
                )

                Spacer(modifier = Modifier.height(18.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (showDial) {
                        TimePicker(
                            state = timePickerState,
                            colors = TimePickerDefaults.colors(
                                timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onTertiary,
                                timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface,
                                periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                periodSelectorSelectedContentColor = MaterialTheme.colorScheme.onTertiary,
                                periodSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surface,
                                periodSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface,
                                selectorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    } else {
                        TimeInput(
                            state = timePickerState,
                            colors = TimePickerDefaults.colors(
                                // SELECTED (Hour/Minute box)
                                timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                timeSelectorSelectedContentColor = MaterialTheme.colorScheme.tertiary,

                                // UNSELECTED (Hour/Minute box)
                                timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.onSecondary,
                                timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface,

                                // AM / PM
                                periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                periodSelectorSelectedContentColor = MaterialTheme.colorScheme.tertiary,
                                periodSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surface,
                                periodSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = accentRed)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            onConfirm(
                                LocalTime.of(
                                    timePickerState.hour,
                                    timePickerState.minute
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentRed,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: Map<String, Any>,
    isMyMessage: Boolean,
    isDeleted: Boolean,
    senderUserID: String,
    receiverUserID: String,
    navController: NavController,
    chatRepositoryViewModel: ChatRepositoryViewModel,
    calendarViewModel: CalendarViewModel,
    chatID: String,
    showTimestamp: Boolean,
    onTapMessage: () -> Unit,
    onHideTimestamp: () -> Unit,
    senderName: String? = null,
    senderPhotoUrl: String? = null,
    onLongPressMine: () -> Unit
) {
    val type = message["type"] as? String ?: "text"
    val text = message["text"] as? String ?: ""
    val messageID = message["messageID"] as? String ?: ""
    val messageChatID = message["chatID"] as? String ?: ""
    val startTime = message["startTime"] as? Timestamp
    val endTime = message["endTime"] as? Timestamp
    val context = LocalContext.current

    val studySessionTitle = message["title"] as? String ?: "Study Session"
    val studySessionDescription = message["description"] as? String ?: ""
    val participants = message["participants"] as? Map<String, String> ?: emptyMap()
    val myStatus = participants[senderUserID] ?: "PENDING"
    val acceptedCount = participants.values.count { it == "ACCEPTED" }
    val declinedCount = participants.values.count { it == "DECLINED" }
    val pendingCount = participants.values.count { it == "PENDING" }
    val convertDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val convertTime = SimpleDateFormat("h:mm a", Locale.getDefault())
    val displayDate = startTime?.toDate()?.let { convertDate.format(it) } ?: ""
    val displayStartTime = startTime?.toDate()?.let { convertTime.format(it) } ?: ""
    val displayEndTime = endTime?.toDate()?.let { convertTime.format(it) } ?: ""

    val bubbleColor = when {
        isDeleted -> DeletedMessageColor
        //isMyMessage -> SenderUserColor
        isMyMessage -> MaterialTheme.colorScheme.tertiaryContainer
        //else -> ReceiverUserColor
        else -> MaterialTheme.colorScheme.primaryContainer
    }

    val bubbleShape = RoundedCornerShape(
        topStart = 22.dp,
        topEnd = 22.dp,
        bottomStart = if (isMyMessage) 22.dp else 8.dp,
        bottomEnd = if (isMyMessage) 8.dp else 22.dp
    )

    val sentAt = message["timestamp"] as? Timestamp
    val timeText = sentAt?.toDate()?.let {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(it)
    }

    LaunchedEffect(showTimestamp) {
        if (showTimestamp) {
            delay(5000)
            onHideTimestamp()
        }
    }

    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = if (isMyMessage) Arrangement.End else Arrangement.Start
    ) {
        if (!isMyMessage) {
            UserAvatar(
                name = senderName ?: "",
                photoUrl = senderPhotoUrl,
                size = 32
            )

            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isMyMessage) Alignment.End else Alignment.Start
        ) {
            if (!isMyMessage) {
                Text(
                    text = senderName ?: "",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (senderName.isNullOrBlank()) Color.Transparent
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp, bottom = 3.dp)
                )
            }
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .background(
                        color = bubbleColor,
                        shape = bubbleShape
                    )
                    .combinedClickable(
                        onClick = {
                            onTapMessage()
                        },
                        onLongClick = {
                            if (isMyMessage && !isDeleted) {
                                onLongPressMine()
                            }
                        }
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                if (isDeleted) {
                    Text(
                        text = "This message was deleted",
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                } else {
                    when (type) {
                        "text" -> {
                            Text(
                                text = text,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.tertiary,
                                lineHeight = 20.sp
                            )
                        }

                        "pin" -> {
                            val pin = message["pin"] as? Map<String, Any> ?: emptyMap()

                            val pinId = pin["id"] as? String ?: ""
                            val pinName = pin["name"] as? String ?: "Shared Pin"
                            val pinCategory = pin["category"] as? String ?: ""
                            val pinBuilding = pin["building"] as? String ?: ""
                            val pinDescription = pin["description"] as? String ?: ""
                            val pinLatitude = (pin["latitude"] as? Number)?.toDouble() ?: 0.0
                            val pinLongitude = (pin["longitude"] as? Number)?.toDouble() ?: 0.0

                            Column {
                                Text(
                                    text = pinName,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontSize = 16.sp
                                )

                                if (pinCategory.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = pinCategory.replaceFirstChar { it.uppercase() },
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }

                                if (pinBuilding.isNotBlank()) {
                                    Text(
                                        text = "Building: $pinBuilding",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }

                                if (pinDescription.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = pinDescription,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        fontSize = 14.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        navController.navigate(
                                            Routes.mapRouteWithPin(
                                                pinId = pinId,
                                                pinName = pinName,
                                                pinCategory = pinCategory,
                                                pinLatitude = pinLatitude,
                                                pinLongitude = pinLongitude,
                                                pinBuilding = pinBuilding,
                                                pinDescription = pinDescription
                                            )
                                        )
                                    },
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("Open Pin")
                                }
                            }
                        }

                        "invitation" -> {
                            Column {
                                Text(
                                    text = studySessionTitle,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontSize = 16.sp
                                )

                                if (studySessionDescription.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = studySessionDescription,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        fontSize = 14.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    "Date: $displayDate",
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontSize = 13.sp
                                )
                                Text(
                                    "Start time: $displayStartTime",
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontSize = 13.sp
                                )
                                Text(
                                    "End time: $displayEndTime",
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontSize = 13.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                if (myStatus == "PENDING" && !isMyMessage) {
                                    Row {
                                        Button(
                                            onClick = {
                                                val title =
                                                    message["title"] as? String ?: "Study Session"
                                                val description =
                                                    message["description"] as? String ?: ""
                                                val startTimestamp =
                                                    message["startTime"] as? Timestamp
                                                val endTimestamp = message["endTime"] as? Timestamp

                                                if (startTimestamp == null || endTimestamp == null) {
                                                    return@Button
                                                }

                                                val timeFormatter =
                                                    DateTimeFormatter.ISO_LOCAL_DATE_TIME

                                                val start = startTimestamp
                                                    .toDate()
                                                    .toInstant()
                                                    .atZone(ZoneId.systemDefault())
                                                    .toLocalDateTime()
                                                    .format(timeFormatter)

                                                val end = endTimestamp
                                                    .toDate()
                                                    .toInstant()
                                                    .atZone(ZoneId.systemDefault())
                                                    .toLocalDateTime()
                                                    .format(timeFormatter)

                                                val calendarEvent = CalendarEvent(
                                                    id = "",
                                                    title = title,
                                                    description = description,
                                                    start = start,
                                                    end = end,
                                                    location = null,
                                                    reminderMinutes = emptyList(),
                                                    isAllDay = false,
                                                    colorHex = "#DC2127",
                                                    source = CalendarSource.LOCAL
                                                )

                                                chatRepositoryViewModel.callRespondStudySessionInvitation(
                                                    chatID = messageChatID,
                                                    messageID = messageID,
                                                    senderUserID = senderUserID,
                                                    invitationResponse = "ACCEPTED"
                                                )

                                                calendarViewModel.saveStudySessionEvent(
                                                    calendarEvent
                                                ) { success ->
                                                    if (success) {
                                                        Toast.makeText(
                                                            context,
                                                            "$title added to your local calendar",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    } else {
                                                        Toast.makeText(
                                                            context,
                                                            "Failed to add $title to your local calendar",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                            },
                                            shape = RoundedCornerShape(14.dp)
                                        ) {
                                            Text("Accept")
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Button(
                                            onClick = {
                                                chatRepositoryViewModel.callRespondStudySessionInvitation(
                                                    chatID = messageChatID,
                                                    messageID = messageID,
                                                    senderUserID = senderUserID,
                                                    invitationResponse = "DECLINED"
                                                )
                                            },
                                            shape = RoundedCornerShape(14.dp)
                                        ) {
                                            Text("Decline")
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                Text(
                                    text = "$acceptedCount accepted • $pendingCount pending • $declinedCount declined",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }
            }

            if (showTimestamp && !timeText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = timeText,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
            }
        }
    }
}