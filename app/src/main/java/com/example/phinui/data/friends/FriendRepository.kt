package com.example.phinui.data.friends

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

data class FriendRequest(
    val id: String = "",
    val fromUid: String = "",
    val fromName: String = "",
    val toUid: String = "",
    val status: String = "pending"
)

class FriendRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun currentUid(): String? = auth.currentUser?.uid

    fun searchUsersByNamePrefix(
        query: String,
        onResult: (List<Pair<String, Map<String, Any>>>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uid = currentUid() ?: return
        val normalized = query.trim().lowercase()

        if (normalized.isBlank()) {
            onResult(emptyList())
            return
        }

        db.collection("users")
            .orderBy("nameLower")
            .startAt(normalized)
            .endAt(normalized + "\uf8ff")
            .get()
            .addOnSuccessListener { snapshot ->
                val users = snapshot.documents
                    .filter { it.id != uid }
                    .mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null
                        doc.id to data
                    }
                onResult(users)
            }
            .addOnFailureListener(onError)
    }

    fun sendFriendRequest(
        toUid: String,
        fromName: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val fromUid = currentUid() ?: return

        // Don't let user send request to self
        if (fromUid == toUid) {
            onError(Exception("You cannot add yourself."))
            return
        }

        // First check both user documents
        db.collection("users").document(fromUid).get()
            .addOnSuccessListener { fromDoc ->

                db.collection("users").document(toUid).get()
                    .addOnSuccessListener { toDoc ->

                        val myFriends = fromDoc.get("friends") as? List<*> ?: emptyList<Any>()
                        val myBlocked = fromDoc.get("blocked") as? List<*> ?: emptyList<Any>()
                        val theirBlocked = toDoc.get("blocked") as? List<*> ?: emptyList<Any>()

                        // Already friends
                        if (myFriends.contains(toUid)) {
                            onError(Exception("You are already friends."))
                            return@addOnSuccessListener
                        }

                        // I blocked them
                        if (myBlocked.contains(toUid)) {
                            onError(Exception("Unblock this user before sending a request."))
                            return@addOnSuccessListener
                        }

                        // They blocked me
                        if (theirBlocked.contains(fromUid)) {
                            onError(Exception("You cannot send a request to this user."))
                            return@addOnSuccessListener
                        }

                        // Check for existing pending request
                        db.collection("friend_requests")
                            .whereEqualTo("fromUid", fromUid)
                            .whereEqualTo("toUid", toUid)
                            .whereEqualTo("status", "pending")
                            .get()
                            .addOnSuccessListener { snapshot ->

                                if (!snapshot.isEmpty) {
                                    onError(Exception("Friend request already sent."))
                                    return@addOnSuccessListener
                                }

                                val request = hashMapOf(
                                    "fromUid" to fromUid,
                                    "fromName" to fromName,
                                    "toUid" to toUid,
                                    "status" to "pending",
                                    "createdAt" to Timestamp.now()
                                )

                                db.collection("friend_requests")
                                    .add(request)
                                    .addOnSuccessListener { onSuccess() }
                                    .addOnFailureListener(onError)
                            }
                            .addOnFailureListener(onError)
                    }
                    .addOnFailureListener(onError)
            }
            .addOnFailureListener(onError)
    }

    fun loadIncomingRequests(
        onResult: (List<Pair<String, FriendRequest>>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uid = currentUid() ?: return

        db.collection("friend_requests")
            .whereEqualTo("toUid", uid)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { snapshot ->
                val items = snapshot.documents.mapNotNull { doc ->
                    val req = doc.toObject(FriendRequest::class.java) ?: return@mapNotNull null
                    doc.id to req.copy(id = doc.id)
                }
                onResult(items)
            }
            .addOnFailureListener(onError)
    }

    fun acceptFriendRequest(
        requestId: String,
        fromUid: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val myUid = currentUid() ?: return

        val myUserRef = db.collection("users").document(myUid)
        val otherUserRef = db.collection("users").document(fromUid)
        val requestRef = db.collection("friend_requests").document(requestId)

        db.runBatch { batch ->
            batch.update(myUserRef, "friends", FieldValue.arrayUnion(fromUid))
            batch.update(otherUserRef, "friends", FieldValue.arrayUnion(myUid))

            batch.update(requestRef, "status", "accepted")
        }
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onError)
    }

    fun declineFriendRequest(
        requestId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("friend_requests")
            .document(requestId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onError)
    }

    fun blockUser(
        blockedUid: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val myUid = currentUid() ?: return

        val myUserRef = db.collection("users").document(myUid)
        val otherUserRef = db.collection("users").document(blockedUid)

        db.collection("friend_requests")
            .whereEqualTo("fromUid", myUid)
            .whereEqualTo("toUid", blockedUid)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { outgoingSnapshot ->

                db.collection("friend_requests")
                    .whereEqualTo("fromUid", blockedUid)
                    .whereEqualTo("toUid", myUid)
                    .whereEqualTo("status", "pending")
                    .get()
                    .addOnSuccessListener { incomingSnapshot ->

                        db.runBatch { batch ->
                            batch.update(myUserRef, "blocked", FieldValue.arrayUnion(blockedUid))
                            batch.update(myUserRef, "friends", FieldValue.arrayRemove(blockedUid))
                            batch.update(otherUserRef, "friends", FieldValue.arrayRemove(myUid))

                            outgoingSnapshot.documents.forEach { doc ->
                                batch.delete(doc.reference)
                            }

                            incomingSnapshot.documents.forEach { doc ->
                                batch.delete(doc.reference)
                            }
                        }.addOnSuccessListener {
                            onSuccess()
                        }.addOnFailureListener(onError)
                    }
                    .addOnFailureListener(onError)
            }
            .addOnFailureListener(onError)
    }

    fun unblockUser(
        blockedUid: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val myUid = currentUid() ?: return

        db.collection("users")
            .document(myUid)
            .update("blocked", FieldValue.arrayRemove(blockedUid))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onError)
    }

    fun listenBlockedUsers(
        onResult: (List<Pair<String, Map<String, Any>>>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration? {
        val uid = currentUid() ?: return null

        return db.collection("users").document(uid)
            .addSnapshotListener { doc, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                val blocked = doc?.get("blocked") as? List<*> ?: emptyList<Any>()

                if (blocked.isEmpty()) {
                    onResult(emptyList())
                    return@addSnapshotListener
                }

                db.collection("users")
                    .whereIn("__name__", blocked.take(10))
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val items = snapshot.documents.mapNotNull { userDoc ->
                            val data = userDoc.data ?: return@mapNotNull null
                            userDoc.id to data
                        }
                        onResult(items)
                    }
                    .addOnFailureListener(onError)
            }
    }

    fun loadFriends(
        onResult: (List<Pair<String, Map<String, Any>>>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uid = currentUid() ?: return

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val friends = doc.get("friends") as? List<*> ?: emptyList<Any>()

                if (friends.isEmpty()) {
                    onResult(emptyList())
                    return@addOnSuccessListener
                }

                db.collection("users")
                    .whereIn("__name__", friends.take(10))
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val items = snapshot.documents.mapNotNull { userDoc ->
                            val data = userDoc.data ?: return@mapNotNull null
                            userDoc.id to data
                        }
                        onResult(items)
                    }
                    .addOnFailureListener(onError)
            }
            .addOnFailureListener(onError)
    }

    fun removeFriend(
        friendUid: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val myUid = currentUid() ?: return

        db.runBatch { batch ->
            val myUserRef = db.collection("users").document(myUid)
            val friendUserRef = db.collection("users").document(friendUid)

            // Remove each other from friends list
            batch.update(myUserRef, "friends", FieldValue.arrayRemove(friendUid))
            batch.update(friendUserRef, "friends", FieldValue.arrayRemove(myUid))
        }
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onError)
    }

    fun listenIncomingRequests(
        onResult: (List<Pair<String, FriendRequest>>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration? {
        val uid = currentUid() ?: return null

        return db.collection("friend_requests")
            .whereEqualTo("toUid", uid)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                val items = snapshot?.documents?.mapNotNull { doc ->
                    val req = doc.toObject(FriendRequest::class.java) ?: return@mapNotNull null
                    doc.id to req.copy(id = doc.id)
                } ?: emptyList()

                onResult(items)
            }
    }

    fun listenFriends(
        onResult: (List<Pair<String, Map<String, Any>>>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration? {
        val uid = currentUid() ?: return null

        return db.collection("users").document(uid)
            .addSnapshotListener { doc, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                val friends = doc?.get("friends") as? List<*> ?: emptyList<Any>()

                if (friends.isEmpty()) {
                    onResult(emptyList())
                    return@addSnapshotListener
                }

                db.collection("users")
                    .whereIn("__name__", friends.take(10))
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val items = snapshot.documents.mapNotNull { userDoc ->
                            val data = userDoc.data ?: return@mapNotNull null
                            userDoc.id to data
                        }
                        onResult(items)
                    }
                    .addOnFailureListener(onError)
            }
    }

    fun listenSearchUsersByNamePrefix(
        query: String,
        onResult: (List<Pair<String, Map<String, Any>>>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration? {
        val uid = currentUid() ?: return null
        val normalized = query.trim().lowercase()

        if (normalized.isBlank()) {
            onResult(emptyList())
            return null
        }

        return db.collection("users")
            .orderBy("nameLower")
            .startAt(normalized)
            .endAt(normalized + "\uf8ff")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                val users = snapshot?.documents
                    ?.filter { it.id != uid }
                    ?.mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null
                        doc.id to data
                    } ?: emptyList()

                onResult(users)
            }
    }
}