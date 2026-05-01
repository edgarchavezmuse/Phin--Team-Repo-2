package com.example.phinui.components.messages

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue

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
        chatsCollection
            .document(chatID)
            .collection("messages")
            .document(messageID)
            .update(
                mapOf(
                    "deleted" to true,
                    "text" to "This message was deleted"
                )
            )
    }

    fun sendMessageRequest(senderUserID: String, receiverUserID: String) {
        val messageProperties = messageInfoHelper(senderUserID, receiverUserID)

        messageProperties.chatReference.update(
            mapOf(
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

                    if (deletedBy.contains(userID)) return@mapNotNull null

                    data + mapOf("chatID" to doc.id)
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
}