package com.example.phinui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import com.example.phinui.components.messages.User
import com.example.phinui.data.friends.FriendRepository

class FriendRepositoryViewModel (private val friendRepository: FriendRepository) : ViewModel() {
    
    private val _friendsList = mutableStateOf<List<User>>(emptyList())
    val friendsList: State<List<User>> = _friendsList

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    init {
        listenForFriendsUpdates()
    }

    private fun listenForFriendsUpdates() {
        friendRepository.listenFriends(
            onResult = { friends ->

                val alphabetizeFriendsList = friends.map {
                    User(
                        uid = it.first,
                        name = it.second["name"] as? String ?: "Unknown",
                        photoUrl = it.second["photoUrl"] as? String
                    )
                }.sortedBy { it.name.lowercase() }

                _friendsList.value = alphabetizeFriendsList

            },
            onError = { error ->
                _errorMessage.value = "Error loading friends: ${error.message}"
            }
        )
    }
}