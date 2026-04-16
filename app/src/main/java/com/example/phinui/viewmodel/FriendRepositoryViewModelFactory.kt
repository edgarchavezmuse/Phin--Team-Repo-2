package com.example.phinui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.phinui.data.friends.FriendRepository

class FriendRepositoryViewModelFactory(private val friendRepository: FriendRepository) : ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FriendRepositoryViewModel::class.java)) {
            return FriendRepositoryViewModel(friendRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}