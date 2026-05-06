package com.example.phinui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

import com.example.phinui.components.messages.UserListRepository
import com.example.phinui.components.messages.User
import com.example.phinui.components.messages.UserState

class UserListViewModel : ViewModel() {

    private val userList = UserListRepository()

    var users by mutableStateOf<List<User>>(emptyList())
        private set

    var sortedUsers by mutableStateOf<List<User>>(emptyList())
    var selectedUser by mutableStateOf<User?>(null)
        private set

    var isLoading by mutableStateOf(true)
        private set

    private val currentUserID = com.google.firebase.auth
        .FirebaseAuth.getInstance()
            .currentUser?.uid ?: ""

    fun loadAllUsers() {
        viewModelScope.launch {
            users = userList.getAllUsers(currentUserID)
            sortedUsers = users.sortedWith(compareBy {it.name.lowercase()})
            isLoading = false
        }
    }

    var userState by mutableStateOf<UserState>(UserState.Empty)
        private set

    fun loadSelectedUser(userID: String) {
        viewModelScope.launch {
            userState = UserState.Loading
            try {
                val user = userList.getUserByID(userID)
                userState = if (user != null) {
                    UserState.Loaded(user)
                } else {
                    UserState.Empty
                }
            } catch (e: Exception) {
                userState = UserState.Empty
            }
        }
    }
}