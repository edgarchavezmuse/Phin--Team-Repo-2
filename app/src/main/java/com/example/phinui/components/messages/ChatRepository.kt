package com.example.phinui.components.messages

import androidx.compose.runtime.mutableStateOf
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.example.phinui.data.CampusLocation

class ChatRepository {

    private val database = FirebaseFirestore.getInstance()
    private val chatsCollection = database.collection("chats")

    //Link users for 1 on 1 messaging
    fun getChatID(firstUserID: String, secondUserID: String): String {
        return if (firstUserID < secondUserID) "${firstUserID}_$secondUserID"
        else "${secondUserID}_$firstUserID"
    }

    fun sendMessage(senderUserID: String, receiverUserID: String, messageText: String) {
        val messageProperties = messageInfoHelper(senderUserID, receiverUserID)

        val batch = database.batch()
        val chatRef = messageProperties.chatReference
        val messagesRef = messageProperties.messageReference.document()

        val message = hashMapOf(
            "type" to "text",
            "senderID" to senderUserID,
            "text" to messageText,
            "timestamp" to messageProperties.currentTime,
            "deleted" to false
        )

        // chat metadata
        val chatData = hashMapOf(
            "type" to "direct",
            "lastMessage" to messageText,
            "lastTimestamp" to messageProperties.currentTime,
            "participants" to listOf(senderUserID, receiverUserID),
            "requestState" to "approved"
        )

        // create message
        batch.set(messagesRef, message)

        // create/update chat
        batch.set(chatRef, chatData, SetOptions.merge())

        // ensure sender's deleted chat state is removed
        batch.update(
            chatRef,
            "deletedBy",
            FieldValue.arrayRemove(senderUserID, receiverUserID)
        )

        // add count for unread messages
        batch.update(
            chatRef,
            mapOf(
                "unreadCounts.$receiverUserID" to FieldValue.increment(1),
                "unreadCounts.$senderUserID" to 0
            )
        )

        // commit everything atomically
        batch.commit()

        setActiveChat(senderUserID, messageProperties.chatID)
    }

    fun sendStudySessionInvitation(
        senderUserID: String,
        receiverUserID: String,
        studySessionTitle: String,
        studySessionDescription: String,
        startTime: Timestamp,
        endTime: Timestamp
    ) {
        val messageProperties = messageInfoHelper(senderUserID, receiverUserID)

        val studySessionInvitation = hashMapOf(
            "type" to "invitation",
            "senderID" to senderUserID,
            "title" to studySessionTitle,
            "description" to studySessionDescription,
            "timestamp" to messageProperties.currentTime,
            "deleted" to false,
            "startTime" to startTime,
            "endTime" to endTime,
            "participants" to mapOf(
                senderUserID to "ACCEPTED",
                receiverUserID to "PENDING"
            )
        )

        messageProperties.messageReference.add(studySessionInvitation)

        setActiveChat(senderUserID, messageProperties.chatID)

    }

    fun respondStudySessionInvitation(
        chatID: String,
        messageID: String,
        senderUserID: String,
        invitationResponse: String
    ) {
        chatsCollection
            .document(chatID)
            .collection("messages")
            .document(messageID)
            .update("participants.$senderUserID", invitationResponse)

        setActiveChat(senderUserID, chatID)

    }

    fun deleteMessage(chatID: String, messageID: String) {
        val chatRef = chatsCollection.document(chatID)
        val messageRef = chatRef.collection("messages").document(messageID)

        messageRef.update(
            mapOf(
                "deleted" to true,
                "text" to "This message was deleted",
                "pin" to FieldValue.delete()
            )
        ).addOnSuccessListener {
            refreshChatPreview(chatID)
        }
    }

    private fun refreshChatPreview(chatID: String) {
        val chatRef = chatsCollection.document(chatID)

        chatRef.collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->

                val lastActiveMessage = snapshot.documents.firstOrNull { doc ->
                    val deleted = doc.getBoolean("deleted") ?: false
                    !deleted
                }

                if (lastActiveMessage != null) {
                    val type = lastActiveMessage.getString("type") ?: "text"
                    val timestamp = lastActiveMessage.getTimestamp("timestamp")

                    val previewText = when (type) {
                        "text" -> lastActiveMessage.getString("text") ?: ""
                        "pin" -> {
                            val pin = lastActiveMessage.get("pin") as? Map<*, *>
                            val pinName = pin?.get("name") as? String
                            if (!pinName.isNullOrBlank()) {
                                "Shared a pin: $pinName"
                            } else {
                                "Shared a pin"
                            }
                        }
                        "invitation" -> {
                            val title = lastActiveMessage.getString("title")
                            if (!title.isNullOrBlank()) {
                                "Study session: $title"
                            } else {
                                "Sent a study session invitation"
                            }
                        }
                        else -> lastActiveMessage.getString("text") ?: ""
                    }

                    chatRef.update(
                        mapOf(
                            "lastMessage" to previewText,
                            "lastTimestamp" to timestamp
                        )
                    )
                } else {
                    chatRef.update(
                        mapOf(
                            "lastMessage" to "",
                            "lastTimestamp" to null,
                         )
                    )
                }
            }
    }

    fun sendMessageRequest(senderUserID: String, receiverUserID: String) {
        val messageProperties = messageInfoHelper(senderUserID, receiverUserID)

        messageProperties.chatReference.update(
            mapOf(
                "type" to "direct",
                "lastMessage" to "",
                "lastTimestamp" to messageProperties.currentTime,
                "participants" to listOf(senderUserID, receiverUserID),
                "senderID" to senderUserID,
                "isFriendChat" to false,
                "requestState" to "pending"
            )
        ).addOnFailureListener {
            messageProperties.chatReference.set(
                mapOf(
                    "type" to "direct",
                    "lastMessage" to "",
                    "lastTimestamp" to messageProperties.currentTime,
                    "participants" to listOf(senderUserID, receiverUserID),
                    "senderID" to senderUserID,
                    "isFriendChat" to false,
                    "requestState" to "pending"
                )
            )
        }
    }

    fun approveMessageRequest(
        chatID: String,
        senderUserID: String,
        receiverUserID: String
    ) {
        chatsCollection.document(chatID)
            .update(
                mapOf(
                    "requestState" to "approved",
                    "deletedBy" to FieldValue.arrayRemove(senderUserID, receiverUserID)
                )
            )
    }

    fun denyMessageRequest(chatID: String) {
        chatsCollection.document(chatID)
            .update(mapOf("requestState" to "none"))
    }

    fun checkForNewMessage(senderUserID: String, receiverUserID: String, newMessage: (List<Map<String, Any>>) -> Unit) {
        val messageProperties = messageInfoHelper(senderUserID, receiverUserID)

        //Retrieving newest message
        messageProperties.messageReference
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener {snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val messages = snapshot.documents.map { doc ->
                    doc.data!!.toMutableMap().apply {
                        put("messageID", doc.id)
                        put("chatID", messageProperties.chatID)
                    }
                }
                newMessage(messages)
            }
    }

    fun listenMessageRequest(
        userID: String,
        onResult: (List<Map<String, Any>>) -> Unit
    ) {
        chatsCollection
            .whereArrayContains("participants", userID)
            .whereEqualTo("requestState", "pending")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val messageRequest = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null

                    val senderID = data["senderID"] as? String
                    if (senderID == userID) return@mapNotNull null

                    data + mapOf("chatID" to doc.id)
                }
                onResult(messageRequest)
            }
    }

    fun listenChats(
        userID: String,
        onResult: (List<Map<String, Any>>) -> Unit
    ) {
        chatsCollection
            .whereArrayContains("participants", userID)
            .whereEqualTo("requestState", "approved")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val chats = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    val deletedBy = data["deletedBy"] as? List<String> ?: emptyList()
                    val mutedBy = data["mutedBy"] as? List<String> ?: emptyList()
                    val isMuted = mutedBy.contains(userID)

                    if (deletedBy.contains(userID)) return@mapNotNull null

                    data + mapOf(
                        "chatID" to doc.id,
                        "isMuted" to isMuted
                        )
                }
                onResult(chats)
            }
    }

    fun setActiveChat(userID: String, chatID: String?) {
        val userRef = database
            .collection("users")
            .document(userID)

        userRef.set(
            mapOf(
                "activeChatID" to chatID,
                "lastActive" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        )
    }

    fun unreadCountChats(userID: String, chatID: String) {
        val unreadCountRef = database
            .collection("chats")
            .document(chatID)

        unreadCountRef.update("unreadCounts.$userID", 0)
    }

    fun deleteChat(userID: String, chatID: String) {
        val chatRef = chatsCollection.document(chatID)

        chatRef.update("deletedBy", FieldValue.arrayUnion(userID))
            .addOnSuccessListener {
                chatRef.get().addOnSuccessListener { snapshot ->

                    val deletedBy = snapshot.get("deletedBy") as? List<String> ?: emptyList()
                    val participants = snapshot.get("participants") as? List<String> ?: emptyList()
                    val isFriendChat = snapshot.getBoolean("isFriendChat") ?: false

                    val bothDeleted = participants.all { deletedBy.contains(it) }

                    if (bothDeleted && !isFriendChat) {
                        chatRef.update("requestState", "none")
                    }
                }
            }
    }

    fun muteChat(userID: String, chatID: String) {
        val chatRef = chatsCollection.document(chatID)

        chatRef.update("mutedBy", FieldValue.arrayUnion(userID))
    }

    fun unmuteChat(userID: String, chatID: String) {
        val chatRef = chatsCollection.document(chatID)

        chatRef.update("mutedBy", FieldValue.arrayRemove(userID))
    }

    data class MessageInfo(
        val chatID: String,
        val chatReference: DocumentReference,
        val messageReference: CollectionReference,
        val currentTime: com.google.firebase.Timestamp
        )
    fun messageInfoHelper (senderUserID: String, receiverUserID: String): MessageInfo {
        val chatID = getChatID(senderUserID, receiverUserID)
        val chatReferenceDocument = chatsCollection.document(chatID)
        val messageReferenceCollection = chatReferenceDocument.collection("messages")
        val currentTime = com.google.firebase.Timestamp.now()

        return MessageInfo(chatID, chatReferenceDocument, messageReferenceCollection, currentTime)
    }
    fun sendPinMessage(
        senderUserID: String,
        receiverUserID: String,
        location: CampusLocation
    ) {
        val messageProperties = messageInfoHelper(senderUserID, receiverUserID)

        val batch = database.batch()
        val chatRef = messageProperties.chatReference
        val messageRef = messageProperties.messageReference.document()

        val pinPayload = hashMapOf(
            "id" to location.id,
            "name" to location.name,
            "category" to location.category,
            "latitude" to location.latitude,
            "longitude" to location.longitude,
            "building" to location.building,
            "description" to location.description,
            "isActive" to location.isActive,
            "source" to "campus"
        )

        val message = hashMapOf(
            "type" to "pin",
            "senderID" to senderUserID,
            "text" to "",
            "timestamp" to messageProperties.currentTime,
            "deleted" to false,
            "pin" to pinPayload
        )

        val chatData = hashMapOf(
            "type" to "direct",
            "lastMessage" to "Shared a pin: ${location.name}",
            "lastTimestamp" to messageProperties.currentTime,
            "participants" to listOf(senderUserID, receiverUserID),
            "requestState" to "approved"
        )

        batch.set(messageRef, message)
        batch.set(chatRef, chatData, SetOptions.merge())
        batch.update(
            chatRef,
            "deletedBy",
            FieldValue.arrayRemove(senderUserID, receiverUserID)
        )

        batch.commit()

        setActiveChat(senderUserID, messageProperties.chatID)
    }

    fun createGroupChat(
        creatorUserID: String,
        participantIDs: List<String>,
        groupName: String,
        onCreated: (String) -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        val chatRef = chatsCollection.document()
        val chatID = chatRef.id
        val currentTime = Timestamp.now()

        val allParticipants = (participantIDs + creatorUserID).distinct()

        val chatData = hashMapOf(
            "type" to "group",
            "groupCategory" to "friends",
            "groupName" to groupName,
            "createdBy" to creatorUserID,
            "participants" to allParticipants,
            "lastMessage" to "",
            "lastTimestamp" to currentTime,
            "requestState" to "approved"
        )

        chatRef.set(chatData)
            .addOnSuccessListener {
                onCreated(chatID)
            }
            .addOnFailureListener { error ->
                onError(error)
            }
    }

    fun sendGroupMessage(
        senderUserID: String,
        chatID: String,
        messageText: String
    ) {
        val chatRef = chatsCollection.document(chatID)
        val messageRef = chatRef.collection("messages").document()
        val currentTime = Timestamp.now()

        chatRef.get()
            .addOnSuccessListener { snapshot ->
                val participants = snapshot.get("participants") as? List<String> ?: emptyList()

                val message = hashMapOf(
                    "type" to "text",
                    "senderID" to senderUserID,
                    "text" to messageText,
                    "timestamp" to currentTime,
                    "deleted" to false
                )

                val chatData = hashMapOf(
                    "lastMessage" to messageText,
                    "lastTimestamp" to currentTime
                )

                val batch = database.batch()

                batch.set(messageRef, message)
                batch.set(chatRef, chatData, SetOptions.merge())

                if (participants.isNotEmpty()) {
                    batch.update(
                        chatRef,
                        "deletedBy",
                        FieldValue.arrayRemove(*participants.toTypedArray())
                    )
                }

                val unreadMessagesAllParticipants = mutableMapOf<String, Any>()

                participants.forEach {participantID ->
                    unreadMessagesAllParticipants["unreadCounts.$participantID"] =
                        if (participantID == senderUserID) {
                            0
                        }
                        else {
                            FieldValue.increment(1)
                        }
                }

                batch.update(chatRef, unreadMessagesAllParticipants)

                batch.commit()
                    .addOnSuccessListener {
                        setActiveChat(senderUserID, chatID)
                    }
            }
    }

    fun checkMessagesByChatID(
        chatID: String,
        newMessage: (List<Map<String, Any>>) -> Unit
    ) {
        chatsCollection
            .document(chatID)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val messages = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null

                    data.toMutableMap().apply {
                        put("messageID", doc.id)
                        put("chatID", chatID)
                    }
                }

                newMessage(messages)
            }
    }

    fun sendGroupStudySessionInvitation(
        senderUserID: String,
        chatID: String,
        studySessionTitle: String,
        studySessionDescription: String,
        startTime: Timestamp,
        endTime: Timestamp
    ) {
        val chatRef = chatsCollection.document(chatID)
        val messageRef = chatRef.collection("messages").document()
        val currentTime = Timestamp.now()

        chatRef.get()
            .addOnSuccessListener { snapshot ->
                val participants = snapshot.get("participants") as? List<String> ?: emptyList()

                val participantStatuses = participants.associateWith { userID ->
                    if (userID == senderUserID) "ACCEPTED" else "PENDING"
                }

                val invitation = hashMapOf(
                    "type" to "invitation",
                    "senderID" to senderUserID,
                    "title" to studySessionTitle,
                    "description" to studySessionDescription,
                    "timestamp" to currentTime,
                    "deleted" to false,
                    "startTime" to startTime,
                    "endTime" to endTime,
                    "participants" to participantStatuses
                )

                val chatData = hashMapOf(
                    "lastMessage" to "Study session: $studySessionTitle",
                    "lastTimestamp" to currentTime
                )

                val batch = database.batch()
                batch.set(messageRef, invitation)
                batch.set(chatRef, chatData, SetOptions.merge())

                if (participants.isNotEmpty()) {
                    batch.update(
                        chatRef,
                        "deletedBy",
                        FieldValue.arrayRemove(*participants.toTypedArray())
                    )
                }

                batch.commit()
                    .addOnSuccessListener {
                        setActiveChat(senderUserID, chatID)
                    }
            }
    }

}