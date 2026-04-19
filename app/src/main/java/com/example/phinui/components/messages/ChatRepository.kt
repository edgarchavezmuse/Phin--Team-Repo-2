package com.example.phinui.components.messages

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

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

        val message = hashMapOf(
            "senderID" to senderUserID,
            "text" to messageText,
            "timestamp" to messageProperties.currentTime,
            "deleted" to false
        )

        //Store message in the chats collection in firebase
        messageProperties.messageReference.add(message)

        //Preview message
        messageProperties.chatReference.update(
            mapOf(
                "lastMessage" to messageText,
                "lastTimestamp" to messageProperties.currentTime,
                "participants" to listOf(senderUserID, receiverUserID),
                "messageRequestApproved" to true
            )
        //Default info if chat between users doesn't exist yet
        ).addOnFailureListener {
            messageProperties.chatReference.set(
                mapOf(
                    "lastMessage" to messageText,
                    "lastTimestamp" to messageProperties.currentTime,
                    "participants" to listOf(senderUserID, receiverUserID),
                    "messageRequestApproved" to true
                )
            )
        }
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
                "messageRequestApproved" to false,
                "senderID" to senderUserID
            )
        ).addOnFailureListener {
            messageProperties.chatReference.set(
                mapOf(
                    "lastMessage" to "",
                    "lastTimestamp" to messageProperties.currentTime,
                    "participants" to listOf(senderUserID, receiverUserID),
                    "messageRequestApproved" to false,
                    "senderID" to senderUserID
                )
            )
        }
    }

    fun approveMessageRequest(chatID: String) {
        chatsCollection.document(chatID)
            .update("messageRequestApproved", true)
    }

    fun denyMessageRequest(chatID: String) {
        chatsCollection.document(chatID)
            .delete()
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
            .whereEqualTo("messageRequestApproved", false)
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
            .whereEqualTo("messageRequestApproved", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val chats = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    data + mapOf("chatID" to doc.id)
                }
                onResult(chats)
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