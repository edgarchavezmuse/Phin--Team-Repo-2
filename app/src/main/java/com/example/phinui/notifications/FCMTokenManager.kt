package com.example.phinui.notifications

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
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

    fun clearToken() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .update("fcmToken", FieldValue.delete())
            .addOnSuccessListener {
                Log.d("FCM_DEBUG", "Token removed successfully")
            }
            .addOnFailureListener { e ->
                Log.d("FCM_DEBUG", "Failed to remove token: ${e.message}")
            }


    }

    fun deleteDeviceToken() {
        FirebaseMessaging.getInstance().deleteToken()
    }
}