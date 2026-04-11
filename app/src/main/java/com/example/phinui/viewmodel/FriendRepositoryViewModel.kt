package com.example.phinui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import com.example.phinui.data.friends.FriendRepository

class FriendRepositoryViewModel (private val friendRepository: FriendRepository) : ViewModel() {
    private val _friendsList = mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList())
    val friendsList: State<List<Pair<String, Map<String, Any>>>> = _friendsList

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    init {
        listenForFriendsUpdates()
    }

    private fun listenForFriendsUpdates() {
        friendRepository.listenFriends(
            onResult = { friends ->
                _friendsList.value = friends
            },
            onError = { error ->
                _errorMessage.value = "Error loading friends: ${error.message}"
            }
        )
    }
}