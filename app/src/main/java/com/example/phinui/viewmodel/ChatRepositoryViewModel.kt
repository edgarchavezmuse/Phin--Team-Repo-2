package com.example.phinui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.phinui.components.messages.ChatRepository

class ChatRepositoryViewModel (
    private val chatRepository: ChatRepository = ChatRepository()
) : ViewModel() {

    val approvedChats = mutableStateOf<List<Map<String, Any>>>(emptyList())
    val messageRequests = mutableStateOf<List<Map<String, Any>>>(emptyList())

    fun loadApprovedChats(userID: String) {
        chatRepository.listenChats(userID) {
            approvedChats.value = it
        }
    }

    fun loadMessageRequest(userID: String) {
        chatRepository.listenMessageRequest(userID) {
            messageRequests.value = it
        }
    }
}