package com.example.phinui.components.messages

import androidx.compose.animation.core.snap
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

data class User(
    val uid: String,
    val name: String
)

class UserListRepository {
    private val database = FirebaseFirestore.getInstance()
    private val usersCollection = database.collection("users")

    val currentUserBlockedList = mutableStateOf<Set<String>>(emptySet())
    val blockedByOtherUsersList = mutableStateOf<Set<String>>(emptySet())


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

//    fun getUserNameByID(
//        userID: String,
//        onResult: (User) -> Unit,
//        onError: (Exception) -> Unit
//    ) {
//
//        database.collection("users")
//            .document(userID)
//            .get()
//            .addOnSuccessListener { document ->
//                if (document.exists()) {
//                    val userName = document.getString("name") ?: "Unknown User"
//                    val user = User(uid = userID, name = userName)
//                    onResult(user)
//            } else {
//                onError(Exception("User not found"))
//                }
//        }.addOnFailureListener { exception ->
//            onError(exception)
//            }
//    }

    suspend fun getUserNameByID(userID: String): String {
        return suspendCancellableCoroutine { continuation ->

            database.collection("users")
                .document(userID)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userName = document.getString("name") ?: "Unknown User"
                        continuation.resume(userName)
                    } else {
                        continuation.resume("Unknown User")
                    }
                }.addOnFailureListener { exception ->
                    continuation.resume("Unknown User")
                }
        }
    }

    fun loadCurrentUserBlockedListListener(currentUserID: String) {
        usersCollection
            .document(currentUserID)
            .addSnapshotListener { snapshot, error ->
                if (error !=null || snapshot == null) return@addSnapshotListener
                val blockedList = (snapshot.get("blocked") as? List<*> ?: emptyList<Any>())
                    .mapNotNull { it as? String }
                    .toSet()
                currentUserBlockedList.value = blockedList
            }
    }

    fun loadBlockedByOtherUsersListListener(currentUserID: String) {
        usersCollection
            .whereArrayContains("blocked", currentUserID)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val blockedList = snapshot.documents
                    .map { it.id }
                    .toSet()
                blockedByOtherUsersList.value = blockedList
            }
    }

}