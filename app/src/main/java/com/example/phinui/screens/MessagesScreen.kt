package com.example.phinui.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.example.phinui.ui.theme.Background
import com.example.phinui.ui.theme.NavText
import com.example.phinui.components.messages.ChatRepository
import com.example.phinui.ui.theme.*
import com.google.android.libraries.places.api.model.kotlin.rectangularBounds

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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = if (isMe) Arrangement.End
                    else Arrangement.Start,
                ) {
                Box(
                    modifier = Modifier
                        .widthIn(max = 250.dp)
                        .background(
                            color = if (isMe) SenderUserColor
                            else ReceiverUserColor
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = text,
                        fontWeight = FontWeight.Bold
                    )
                  }
                }
            }
        }
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
}
