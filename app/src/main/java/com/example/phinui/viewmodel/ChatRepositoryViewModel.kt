package com.example.phinui.viewmodel

import android.util.Log
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.phinui.components.messages.ChatRepository
import com.example.phinui.components.messages.User
import com.example.phinui.components.messages.UserListRepository
import com.example.phinui.data.friends.FriendRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp

class ChatRepositoryViewModel (
    private val chatRepository: ChatRepository = ChatRepository(),
    private val friendRepository: FriendRepository = FriendRepository(),
    val userListRepository: UserListRepository = UserListRepository()
) : ViewModel() {

    val firebaseAuthenticated = FirebaseAuth.getInstance()
    val firebaseFirestoreAuthenticated = FirebaseFirestore.getInstance()
    val currentUserID = firebaseAuthenticated.currentUser?.uid
    val approvedChats = mutableStateOf<List<Map<String, Any>>>(emptyList())
    val messageRequests = mutableStateOf<List<Map<String, Any>>>(emptyList())

    val showMessageApprovedDialog = mutableStateOf(false)
    var activeChatUserName = mutableStateOf<String?>(null)

    val confirmSendMessageRequest = mutableStateOf<String?>(null)

    // FOR FILTERING FRIENDS FROM GENERAL
    val friendsList = mutableStateOf<List<User>>(emptyList())

    val getFriendChats = derivedStateOf {
        val friendIDs = friendsList.value.map { it.uid }.toSet()

        approvedChats.value.filter { chat ->
            val participants = chat["participants"] as? List<*> ?: return@filter false

            val otherUserID = participants
                .mapNotNull { it as? String }
                .firstOrNull { it != currentUserID }

            otherUserID != null && otherUserID in friendIDs
        }
    }

    init {
        friendRepository.listenFriends(
            onResult = { friends ->
                friendsList.value = friends.map {
                    User(
                        uid = it.first,
                        name = it.second["name"] as? String ?: "Unknown"
                    )
                }
                    .sortedBy { it.name.lowercase() }
            }, onError = { exception -> Log.e("Friends", "Error", exception) }
        )
    }

    fun startListening(userID: String) {
        chatRepository.listenChats(userID) { chats ->
            approvedChats.value = chats
        }
    }

    fun callGetChatID(firstUserID: String, secondUserID: String): String {
        return chatRepository.getChatID(firstUserID, secondUserID)
    }

    fun callCheckForNewMessage(senderUserID: String, receiverUserID: String, newMessage: (List<Map<String, Any>>) -> Unit) {
        chatRepository.checkForNewMessage(senderUserID, receiverUserID, newMessage)
    }

    val getGeneralChats = derivedStateOf {
        val friendIDs = friendsList.value.map { it.uid }.toSet()

        val filterOutFriends = approvedChats.value.filter{ chat ->
            val participants = chat["participants"] as? List<*> ?: return@filter false

            val otherUserID = participants
                .mapNotNull { it as? String }
                .firstOrNull{ it != currentUserID }

            otherUserID!= null && otherUserID !in friendIDs
        }

        filterOutFriends.sortedBy { chat ->
            val filteredParticipants = chat["participants"] as? List<*> ?: return@sortedBy ""
            val filteredOtherUserID = filteredParticipants
                .mapNotNull { it as? String }
                .firstOrNull{ it != currentUserID }
            filteredOtherUserID?.lowercase() ?: ""
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
        val currentUserDocument = firebaseFirestoreAuthenticated
            .collection("users")
            .document(senderUserID)

        val targetUserDocument = firebaseFirestoreAuthenticated
            .collection("users")
            .document(receiverUserID)

        firebaseFirestoreAuthenticated.runTransaction { transaction ->
            val currentUserDocumentSnapshot = transaction.get(currentUserDocument)
            val targetUserDocumentSnapshot = transaction.get(targetUserDocument)
            val currentUserBlockedList =
                (currentUserDocumentSnapshot.get("blocked") as? List<*> ?: emptyList<Any>())
                    .mapNotNull { it as? String }
            val targetUserBlockedList =
                (targetUserDocumentSnapshot.get("blocked") as? List<*> ?: emptyList<Any>())
                    .mapNotNull { it as? String }

            if (currentUserBlockedList.contains(receiverUserID)) {
                // exit function
                throw Exception("CURRENT_USER_BLOCKED_TARGET_USER")
            }

            if (targetUserBlockedList.contains(senderUserID)) {
                // exit function
                throw Exception("TARGET_USER_BLOCKED_CURRENT_USER")
            }

            val checkChat = approvedChats.value.firstOrNull { chat ->
                val participants = chat["participants"] as? List<*>
                val userIDs = participants?.mapNotNull { it as? String } ?: emptyList()

                senderUserID in userIDs && receiverUserID in userIDs
            }

            val isMessageRequestApproved =
                checkChat?.get("messageRequestApproved") as? Boolean ?: false
            if (isMessageRequestApproved) {
                return@runTransaction "CHAT_EXISTS"
            }

            return@runTransaction "SEND_REQUEST"
        }
            .addOnSuccessListener { result ->
                when (result) {
                    "CHAT_EXISTS" -> {
                        showMessageApprovedDialog.value = true
                        activeChatUserName.value = receiverUserName
                    }
                    "SEND_REQUEST" -> {
                        chatRepository.sendMessageRequest(senderUserID, receiverUserID)
                        confirmSendMessageRequest.value = "Message request sent"
                    }
                }
            }

            .addOnFailureListener { error ->
            when (error.message) {
                "CURRENT_USER_BLOCKED_TARGET_USER" -> {
                    confirmSendMessageRequest.value = "Unable to send request"
                }
                "TARGET_USER_BLOCKED_CURRENT_USER" -> {
                    confirmSendMessageRequest.value = "Unable to send request"
                }
                else -> {
                    confirmSendMessageRequest.value = "Failed to send request"
                }
            }
        }
    }

    fun callSendMessage(senderUserID: String, receiverUserID: String, messageText: String) {
        chatRepository.sendMessage(senderUserID, receiverUserID, messageText)
    }

    fun callSendStudySessionInvitation(
        senderUserID: String,
        receiverUserID: String,
        studySessionTitle: String,
        studySessionDescription: String,
        startTime: Timestamp,
        endTime: Timestamp

    ) {
        chatRepository.sendStudySessionInvitation(senderUserID, receiverUserID, studySessionTitle, studySessionDescription, startTime, endTime)
    }

    fun callRespondStudySessionInvitation(
        chatID: String,
        messageID: String,
        senderUserID: String,
        invitationResponse: String
    ) {
        chatRepository.respondStudySessionInvitation(chatID, messageID, senderUserID, invitationResponse)
    }

    fun approveRequest(chatID: String) {
        chatRepository.approveMessageRequest(chatID)
    }

    fun denyRequest(chatID: String) {
        chatRepository.denyMessageRequest(chatID)
    }

    fun onDeleteMessage(
        message: Map<String, Any>,
        chatID: String
    ) {
        val senderID = message["senderID"] as? String ?: return
        val messageID = message["messageID"] as? String ?: return
        val currentUserID = currentUserID ?: return

        if (senderID != currentUserID) return

        chatRepository.deleteMessage(chatID, messageID)
    }

    fun onChatOpened(userID: String, chatID: String) {
        chatRepository.setActiveChat(userID, chatID)
    }

    fun onChatClosed(userID: String) {
        chatRepository.setActiveChat(userID, null)
    }

    fun onDeleteChat(userID: String, chatID: String) {
        chatRepository.deleteChat(userID, chatID)
    }

}