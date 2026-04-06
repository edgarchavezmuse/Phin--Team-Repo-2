package com.example.phinui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

import com.example.phinui.components.messages.UserListRepository
import com.example.phinui.components.messages.User

class UserListViewModel : ViewModel() {

    private val userList = UserListRepository()

    var users by mutableStateOf<List<User>>(emptyList())
        private set

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
            isLoading = false
        }
    }

    fun loadSelectedUser(userID: String) {
        viewModelScope.launch {
            isLoading = true
            selectedUser = userList.getUserByID(userID)
            isLoading = false
        }
    }
}