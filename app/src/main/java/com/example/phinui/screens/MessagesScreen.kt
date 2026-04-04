package com.example.phinui.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.*
import com.example.phinui.ui.theme.Background
import com.example.phinui.ui.theme.NavText
import com.example.phinui.components.messages.ChatRepository

@Composable
fun MessagesScreen(senderUserID: String, receiverUserID: String) {
    val chatRepository = remember { ChatRepository() }
    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf<Map<String, Any>>()) }

    LaunchedEffect(senderUserID, receiverUserID) {
        chatRepository.checkForNewMessage(senderUserID, receiverUserID) {
            newMessages ->
            messages = newMessages
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            reverseLayout = false,
            verticalArrangement = Arrangement.Top
        ) {
            items(messages) { message ->
                val text = message["text"] as? String?: ""
                val senderID = message["senderID"] as? String?: ""
                val isMe = senderID == senderUserID

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                        .background(
                            if (isMe) Color(0xFFDCF8C6)
                            else Color(0xFFFFFFFF)
                        )
                        .padding(8.dp)
                ) {
                    Text (
                        text = text
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BasicTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier
                    .weight(1f)
                    .background(Color.LightGray)
                    .padding(8.dp)
            )

            Button(onClick = {
                if (messageText.isNotBlank()) {
                    chatRepository.sendMessage(
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
        }
    }
    //Box(
    //    modifier = Modifier
    //        .fillMaxSize()
    //        .background(Background),
    //    contentAlignment = Alignment.Center
    //) {
    //    Text(
    //        text = "Messages Screen",
    //        fontSize = 24.sp,
    //        color = NavText
    //    )
    //}
}
