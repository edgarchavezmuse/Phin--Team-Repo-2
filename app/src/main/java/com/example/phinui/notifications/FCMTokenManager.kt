package com.example.phinui.notifications

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging

object FCMTokenManager {

    fun registerToken() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            val data = mapOf("fcmToken" to token)

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .set(data, SetOptions.merge())
        }
    }
}