package com.example.phinui.notifications

import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.phinui.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PhinFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // send the token to the database
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .set(mapOf("fcmToken" to token), SetOptions.merge())
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("FCM_DEBUG", "DATA: ${remoteMessage.data}")
        Log.d("FCM_DEBUG", "TYPE: ${remoteMessage.data["type"]}")
        val title = remoteMessage.data["title"] ?: "Notification"
        val body = remoteMessage.data["body"] ?: ""
        val type = remoteMessage.data["type"]
        val uri = remoteMessage.data["uri"]

        val launchIntent = when (type) {

            "FRIEND_REQUEST" ->
                Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }

            "FRIEND_ACCEPTED" ->
                Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }

            else ->
                Intent(this, MainActivity::class.java)


        }

        NotificationHelper.showNotification(
            context = this,
            type = NotificationType.FRIEND_REQUESTS,
            title = title,
            body = body,
            intent = launchIntent,
            requestCode = System.currentTimeMillis().toInt()
        )
    }
}

