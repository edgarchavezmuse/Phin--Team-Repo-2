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
            "timestamp" to messageProperties.currentTime
        )

        //Store message in the chats collection in firebase
        messageProperties.messageReference.add(message)

        //Preview message
        messageProperties.chatReference.update(
            mapOf(
                "lastMessage" to messageText,
                "lastTimestamp" to messageProperties.currentTime,
                "participants" to listOf(senderUserID, receiverUserID)
            )
        //Default info if chat between users doesn't exist yet
        ).addOnFailureListener {
            messageProperties.chatReference.set(
                mapOf(
                    "lastMessage" to messageText,
                    "lastTimestamp" to messageProperties.currentTime,
                    "participants" to listOf(senderUserID, receiverUserID)
                )
            )
        }
    }

    fun checkForNewMessage(senderUserID: String, receiverUserID: String, newMessage: (List<Map<String, Any>>) -> Unit) {
        val messageProperties = messageInfoHelper(senderUserID, receiverUserID)

        //Retrieving newest message
        messageProperties.messageReference
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener {snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                //val messages = snapshot.documents.map {it.data!!}
                val messages = snapshot?.documents?.mapNotNull {it.data} ?: emptyList()
                newMessage(messages)
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