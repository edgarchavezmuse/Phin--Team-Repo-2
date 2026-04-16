package com.example.phinui.components.messages

import androidx.compose.animation.core.snap
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class User(
    val uid: String,
    val name: String
)

class UserListRepository {
    private val database = FirebaseFirestore.getInstance()
    private val usersCollection = database.collection("users")

    suspend fun getAllUsers(currentUserID: String): List<User> {
        return try {
            val snapshot = usersCollection
                .orderBy("name")
                .get()
                .await()
            snapshot.documents
                .mapNotNull { document ->
                    val uid = document.id
                    val name = document.getString("name") ?: return@mapNotNull null
                    if (uid != currentUserID) User(uid, name)
                    else null
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUserByID(userID: String): User? {
        return try{
            val document = usersCollection.document(userID)
                .get()
                .await()
            val userName = document.getString("name") ?: return null
            User(userID, userName)
        } catch (e: Exception) {
            null
        }
    }
}