package com.example.phinui.data.schedule

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ScheduleRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun addClass(scheduleClass: ScheduleClass) {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("User is not logged in")

        db.collection("users")
            .document(uid)
            .collection("schedule")
            .add(scheduleClass)
            .await()
    }

    suspend fun getClasses(): List<ScheduleClass> {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("User is not logged in")

        val snapshot = db.collection("users")
            .document(uid)
            .collection("schedule")
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(ScheduleClass::class.java)?.copy(id = doc.id)
        }
    }

    suspend fun deleteClass(scheduleClass: ScheduleClass) {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("User is not logged in")

        if (scheduleClass.id.isBlank()) {
            throw IllegalStateException("Class ID is missing")
        }

        db.collection("users")
            .document(uid)
            .collection("schedule")
            .document(scheduleClass.id)
            .delete()
            .await()
    }
}