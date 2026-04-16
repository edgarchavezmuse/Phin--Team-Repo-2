package com.example.phinui.viewmodel

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phinui.components.messages.ChatRepository
import com.example.phinui.components.messages.User
import com.example.phinui.components.messages.UserListRepository
import com.example.phinui.data.friends.FriendRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatRepositoryViewModel (
    private val chatRepository: ChatRepository = ChatRepository(),
    private val friendRepository: FriendRepository = FriendRepository(),
    val userListRepository: UserListRepository = UserListRepository()
) : ViewModel() {

    val firebaseAuthenticated = FirebaseAuth.getInstance()
    val firebaseFirestoreAuthenticated = FirebaseFirestore.getInstance()
    val currentUserID = firebaseAuthenticated.currentUser?.uid
    //val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
    val approvedChats = mutableStateOf<List<Map<String, Any>>>(emptyList())
    val messageRequests = mutableStateOf<List<Map<String, Any>>>(emptyList())

    val showMessageApprovedDialog = mutableStateOf(false)
    var activeChatUserName = mutableStateOf<String?>(null)

    val confirmSendMessageRequest = mutableStateOf<String?>(null)

    // FOR FILTERING FRIENDS FROM GENERAL
    val friendsList = mutableStateOf<List<User>>(emptyList())

    init {
        friendRepository.listenFriends(
            onResult = { friends ->
                friendsList.value = friends.map {
                    User(
                        uid = it.first,
                        name = it.second["name"] as? String ?: "Unknown"
                    )
                }
            }, onError = { exception -> Log.e("Friends", "Error", exception) }
        )
    }

    fun startListening(userID: String) {
        chatRepository.listenChats(userID) { chats ->
            approvedChats.value = chats
        }
    }

    val getGeneralChats = derivedStateOf {
        val friendIDs = friendsList.value.map { it.uid }.toSet()

        approvedChats.value.filter{ chat ->
            val participants = chat["participants"] as? List<*> ?: return@filter false

            val otherUserID = participants
                .mapNotNull { it as? String }
                .firstOrNull{ it != currentUserID }

            otherUserID!= null && otherUserID !in friendIDs
        }
    }

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

    fun sendMessageRequest(senderUserID: String, receiverUserID: String, receiverUserName: String?) {
        val checkChat = approvedChats.value.firstOrNull{ chat ->
            val participants = chat["participants"] as? List<*>
            val userIDs = participants?.mapNotNull { it as? String} ?: emptyList()

            senderUserID in userIDs && receiverUserID in userIDs
        }

        val isMessageRequestApproved = checkChat?.get("messageRequestApproved") as? Boolean ?: false
        if (isMessageRequestApproved) {
            showMessageApprovedDialog.value = true
            activeChatUserName.value = receiverUserName
        }
        else {
            try {
                chatRepository.sendMessageRequest(senderUserID, receiverUserID)
                confirmSendMessageRequest.value = "Message request sent"
            } catch (e: Exception) {
                confirmSendMessageRequest.value = e.message ?: "Failed to send request."
            }
        }
    }

    fun approveRequest(chatID: String) {
        chatRepository.approveMessageRequest(chatID)
    }

    fun denyRequest(chatID: String) {
        chatRepository.denyMessageRequest(chatID)
    }

}