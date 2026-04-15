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

    //val requestUsers = mutableStateOf<List<User>>(emptyList())

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

//    fun loadRequestUsers(currentUserID: String) {
//        chatRepository.listenMessageRequest(currentUserID) { chats ->
//            viewModelScope.launch {
//                //val users = chats.mapNotNull { chat ->
//                val users = mutableListOf<User>()
//                for (chat in chats) {
//                    val participants =
//                        //chat["participants"] as? List<String> ?: return@mapNotNull null
//                        chat["participants"] as? List<String> ?: continue
//
//                    val otherUserID =
//                        //participants.firstOrNull { it != currentUserID } ?: return@mapNotNull null
//                        participants.firstOrNull { it != currentUserID } ?: continue
//                    //getUserNameByID(otherUserID)
//                    userListRepository.getUserNameByID(otherUserID, { user ->
//                        users.add(user)
//                    }, { exception ->
//                        println("Error fetching user: ${exception.message}")
//                    })
//                    requestUsers.value = users
//                }
//            }
//        }
//    }

    fun sendMessageRequest(senderUserID: String, receiverUserID: String) {
        chatRepository.sendMessageRequest(senderUserID, receiverUserID)
    }

    fun approveRequest(chatID: String) {
        chatRepository.approveMessageRequest(chatID)
    }

    fun denyRequest(chatID: String) {
        chatRepository.denyMessageRequest(chatID)
    }

//    fun getUserNameByID(
//        userID: String,
//        onResult: (User) -> Unit,
//        onError: (Exception) -> Unit) {
//
//        firebaseFirestoreAuthenticated.collection("users").document(userID)
//            .get()
//            .addOnSuccessListener { document ->
//                if (document.exists()) {
//                    val userName = document.getString("name") ?: "Unknown User"
//                    val user = User(uid = userID, name = userName)
//                    onResult(user)
//            } else {
//                onError(Exception("User not found"))
//                }
//        }.addOnFailureListener { exception ->
//            onError(exception)
//            }
//    }
}