package com.example.phinui.data.calendar

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await

data class GoogleCalendarIntegration(
    val connected: Boolean = false,
    val googleEmail: String? = null
)

class GoogleCalendarFirebaseRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun doc(uid: String) =
        db.collection("users")
            .document(uid)
            .collection("integrations")
            .document("googleCalendar")

    suspend fun saveConnection(uid: String, email: String?) {
        val data = mapOf(
            "connected" to true,
            "googleEmail" to email,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        doc(uid).set(data).await()
    }

    suspend fun clearConnection(uid: String) {
        val data = mapOf(
            "connected" to false,
            "googleEmail" to null,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        doc(uid).set(data).await()
    }

    suspend fun getConnection(uid: String): GoogleCalendarIntegration? {
        val snapshot = doc(uid).get().await()
        if (!snapshot.exists()) return null

        return GoogleCalendarIntegration(
            connected = snapshot.getBoolean("connected") == true,
            googleEmail = snapshot.getString("googleEmail")
        )
    }
}