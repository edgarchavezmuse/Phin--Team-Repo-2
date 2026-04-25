package com.example.phinui.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
//import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.phinui.components.messages.ChatRepository
import com.example.phinui.ui.theme.*
import com.example.phinui.viewmodel.ChatRepositoryViewModel
import com.example.phinui.viewmodel.UserListViewModel
import com.google.firebase.Timestamp
import java.time.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


@Composable
fun MessagesScreen(
    senderUserID: String,
    receiverUserID: String,
    userListViewModel: UserListViewModel = viewModel(),
    setTopBarTitle: (String) -> Unit
) {
    val chatRepository = remember { ChatRepository() }
    val chatRepositoryViewModel = remember { ChatRepositoryViewModel() }
    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf<Map<String, Any>>()) }

    val selectedUser = userListViewModel.selectedUser
    val isLoadingStatus = userListViewModel.isLoading
    val autoScrollState = rememberLazyListState()
    val initialChatOpen = remember { mutableStateOf(true) }

    var selectedMessage = remember { mutableStateOf<Map<String, Any>?>(null) }
    var showDeleteDialog = remember { mutableStateOf(false) }
    //val chatID = chatRepository.getChatID(senderUserID, receiverUserID)
    val chatID = chatRepositoryViewModel.callGetChatID(senderUserID, receiverUserID)

    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    var selectedStudyDate by remember { mutableStateOf("") }
    var selectedStudyStartTime by remember { mutableStateOf<LocalTime?>(null) }
    var selectedStudyEndTime by remember { mutableStateOf<LocalTime?>(null)}
//    var selectedStudyStartHour by remember { mutableStateOf<Int?>(null) }
//    var selectedStudyStartMinute by remember { mutableStateOf<Int?>(null) }
//    var selectedStudyEndHour by remember { mutableStateOf<Int?>(null) }
//    var selectedStudyEndMinute by remember { mutableStateOf<Int?>(null) }

    //var selectedStudyTime by remember { mutableStateOf("") }
    //var eventDate by remember { mutableStateOf("") }

    LaunchedEffect(selectedUser) {
        selectedUser?.name?.let { userName ->
            setTopBarTitle("Chatting with $userName")}
    }

    LaunchedEffect(senderUserID, receiverUserID) {
        //chatRepository.checkForNewMessage(senderUserID, receiverUserID) {
        chatRepositoryViewModel.callCheckForNewMessage(senderUserID, receiverUserID) {
                newMessages ->
            messages = newMessages
        }
    }

    LaunchedEffect(receiverUserID) {
        userListViewModel.loadSelectedUser(receiverUserID)
    }

    LaunchedEffect(chatID) {
        chatRepositoryViewModel.onChatOpened(senderUserID, chatID)
    }


    DisposableEffect(Unit) {
        onDispose {
            chatRepositoryViewModel.onChatClosed(senderUserID)
        }
    }

    if (initialChatOpen.value) {
        //Automatic scroll effect
        LaunchedEffect(messages) {
            if (messages.isNotEmpty()) {
                autoScrollState.animateScrollToItem(messages.size - 1)
                initialChatOpen.value = false
            }
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (isLoadingStatus) {
            CircularProgressIndicator()
        }

        if (showDeleteDialog.value && selectedMessage.value != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog.value = false
                    selectedMessage.value = null
                },
                title = { Text("Delete message?") },
                text = { Text("This message will be deleted for everyone.") },
                confirmButton = {
                    TextButton(onClick = {
                        val message = selectedMessage.value ?: return@TextButton
                        //val chatID = chatRepository.getChatID(senderUserID, receiverUserID)
                        //val chatID = chatRepositoryViewModel.callGetChatID(senderUserID, receiverUserID)
                        chatRepositoryViewModel.onDeleteMessage(
                            message = message,
                            chatID = chatID
                        )
                        showDeleteDialog.value = false
                        selectedMessage.value = null
                    }) {
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
            verticalArrangement = Arrangement.Top
        ) {
            items(messages) { message ->
                val type = message["type"] as? String?: "text"
                val text = message["text"] as? String ?: ""

                val senderID = message["senderID"] as? String ?: ""
                val isDeleted = message["deleted"] as? Boolean ?: false
                val messageID = message["messageID"] as? String ?: ""
                val chatID = message["chatID"] as? String ?: ""
                val isMyMessage = senderID == senderUserID

                val studySessionTitle = message["title"] as? String ?: "Study Session"
                val studySessionDescription = message["description"] as? String ?: ""
                val participants = message["participants"] as? Map<String, String> ?: emptyMap()
                val myStatus = participants[senderUserID] ?: "PENDING"
                val receiverStatus = participants[receiverUserID] ?: "PENDING"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = if (isMyMessage) Arrangement.End
                    else Arrangement.Start,
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 250.dp)
                            .background(
                                color = when {
                                    isDeleted -> DeletedMessageColor
                                    isMyMessage -> SenderUserColor
                                    else -> ReceiverUserColor
                                }
                            )
                            .padding(12.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = {
                                        if (isMyMessage && !isDeleted) {
                                            selectedMessage.value = message
                                            showDeleteDialog.value = true
                                        }
                                    }
                                )
                            }
                    ) {

                        if (isDeleted) {
                            Text(
                                text = "This message was deleted",
                                fontStyle = FontStyle.Italic
                            )
                        }

                        else {
                            when (type) {
                                "text" -> {
                                    Text(
                                        text = text,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                "invitation" -> {
                                    Column {
                                        Text(
                                            text = studySessionTitle,
                                            fontWeight = FontWeight.Bold
                                        )

                                        if (studySessionDescription.isNotEmpty()) {
                                            Text(
                                                text = studySessionDescription
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        when (myStatus) {
                                            "PENDING" -> {
                                                if (!isMyMessage) {
                                                    Row {
                                                        Button(
                                                            onClick = {
                                                                chatRepositoryViewModel.callRespondStudySessionInvitation(
                                                                    chatID = chatID,
                                                                    messageID = messageID,
                                                                    senderUserID = senderUserID,
                                                                    invitationResponse = "ACCEPTED"
                                                                )
                                                            }
                                                        ) {
                                                            Text("Accept")
                                                        }

                                                        Spacer(modifier = Modifier.height(8.dp))

                                                        Button(
                                                            onClick = {
                                                                chatRepositoryViewModel.callRespondStudySessionInvitation(
                                                                    chatID = chatID,
                                                                    messageID = messageID,
                                                                    senderUserID = senderUserID,
                                                                    invitationResponse = "DECLINED"
                                                                )
                                                            }
                                                        ) {
                                                            Text("Decline")
                                                        }
                                                    }
                                                } else {
                                                    Text("Pending response...")
                                                }
                                            }

                                            "ACCEPTED" -> {
                                                when (receiverStatus) {
                                                    "PENDING" -> {
                                                        Text("Pending response...")
                                                    }
                                                    "ACCEPTED" -> {
                                                        Text("Accepted")
                                                    }
                                                    "DECLINED" -> {
                                                        Text("Declined")
                                                    }
                                                }
                                            }

                                            // This one may never fire
                                            "DECLINED" -> {
                                                if (receiverStatus == "PENDING"){
                                                    Text("Pending response...")
                                                }
                                                else {
                                                    Text("Declined")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Buttons and text field
        var showStudySessionInviteDialog by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BasicTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier
                    .weight(1f)
                    .background(MessageBox)
                    .padding(16.dp),
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    color = Color.Black
                )
            )

            Button(onClick = {
                if (messageText.isNotBlank()) {
                    //chatRepository.sendMessage(
                    chatRepositoryViewModel.callSendMessage(
                        senderUserID,
                        receiverUserID,
                        messageText
                    )
                    messageText = ""
                }
            }
            ) {
                Text ("Send")
            }

            IconButton(onClick = {
                showStudySessionInviteDialog = true
            }
            ) {
                Icon(Icons.Default.Event, contentDescription = "Invite")
            }

            if (showStudySessionInviteDialog) {
                var studySessionTitle by remember { mutableStateOf("") }
                var studySessionDescription by remember {mutableStateOf("")}

                AlertDialog(
                    onDismissRequest = { showStudySessionInviteDialog = false },
                    title = {
                        Text("Create study session")
                    },
                    text = {
                        Column{
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
                                Text("Select date")
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
                                Text("Select start time")
                            }

                            if (showStartTimePicker) {
                                EventTimePickerDialog (
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
                                Icon(Icons.Default.AccessTime, contentDescription = "End time")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Select end time")
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
                        }
                    },

                    confirmButton = {
                        Button(
                            onClick = {
                                if(selectedStudyStartTime == null || selectedStudyEndTime == null) {
                                    return@Button
                                }

                                val studyStartTime = convertToCalendarTimestamp(
                                    selectedStudyDate,
                                    selectedStudyStartTime!!
                                )

                                val studyEndTime = convertToCalendarTimestamp(
                                    selectedStudyDate,
                                    selectedStudyEndTime!!
                                )

                                chatRepositoryViewModel.callSendStudySessionInvitation(
                                    senderUserID = senderUserID,
                                    receiverUserID = receiverUserID,
                                    studySessionTitle = studySessionTitle,
                                    studySessionDescription = studySessionDescription,
                                    startTime = studyStartTime,
                                    endTime = studyEndTime
                                )

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
        }
    }
}

fun convertToCalendarTimestamp(selectedStudyDate: String, selectedStudyTime: LocalTime): Timestamp {
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
    val accentRed = Color(0xFFFF1F1F)

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
                    contentColor = Color.White
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
            containerColor = Color.White
        )
    ) {
        DatePicker(
            state = datePickerState,
            showModeToggle = true,
            colors = DatePickerDefaults.colors(
                containerColor = Color.White,
                titleContentColor = Color.Black,
                headlineContentColor = Color.Black,
                weekdayContentColor = Color.DarkGray,
                subheadContentColor = Color.DarkGray,
                selectedDayContainerColor = accentRed,
                selectedDayContentColor = Color.White,
                todayDateBorderColor = accentRed,
                todayContentColor = accentRed,
                selectedYearContainerColor = accentRed,
                selectedYearContentColor = Color.White,
                dayContentColor = Color.Black,
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

    val accentRed = Color(0xFFFF1F1F)
    val cardColor = Color(0xFFF7F5F2)
    val titleColor = Color(0xFF111111)
    val bodyColor = Color(0xFF444444)

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
                        TimePicker(state = timePickerState)
                    } else {
                        TimeInput(state = timePickerState)
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
                            contentColor = Color.White
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

//private fun convertTo24Hour(time12: String): String {
//    val parts = time12.trim().split(" ")
//    val time = parts[0]
//    val period = parts[1]
//
//    val (hourStr, minuteStr) = time.split(":")
//    var hour = hourStr.toInt()
//
//    if (period == "PM" && hour != 12) {
//        hour += 12
//    } else if (period == "AM" && hour == 12) {
//        hour = 0
//    }
//
//    return String.format("%02d:%02d", hour, minuteStr.toInt())
//}
//
//private fun formatTo12Hour(hour24: Int, minute: Int): String {
//    val period = if (hour24 >= 12) "PM" else "AM"
//    val hour12 = when {
//        hour24 == 0 -> 12
//        hour24 > 12 -> hour24 - 12
//        else -> hour24
//    }
//    return String.format("%d:%02d %s", hour12, minute, period)
//}

//private fun isValidDate(date: String): Boolean {
//    val regex = Regex("""\d{4}-\d{2}-\d{2}""")
//    return regex.matches(date)
//}