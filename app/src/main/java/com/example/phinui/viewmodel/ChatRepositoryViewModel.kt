package com.example.phinui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phinui.components.messages.ChatRepository
import com.example.phinui.components.messages.User
import com.example.phinui.components.messages.UserListRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatRepositoryViewModel (
    private val chatRepository: ChatRepository = ChatRepository(),
    val userListRepository: UserListRepository = UserListRepository()
) : ViewModel() {

    val firebaseAuthenticated = FirebaseAuth.getInstance()
    val firebaseFirestoreAuthenticated = FirebaseFirestore.getInstance()
    val currentUserID = firebaseAuthenticated.currentUser?.uid
    //val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
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

    fun sendMessageRequest(senderUserID: String, receiverUserID: String) {
        chatRepository.sendMessageRequest(senderUserID, receiverUserID)
    }

    fun approveRequest(chatID: String) {
        chatRepository.approveMessageRequest(chatID)
    }

    fun denyRequest(chatID: String) {
        chatRepository.denyMessageRequest(chatID)
    }

}