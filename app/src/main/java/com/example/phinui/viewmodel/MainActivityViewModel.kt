package com.example.phinui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MainActivityViewModel : ViewModel() {
    var topBarTitle by mutableStateOf("")
        private set

    var isMessagesScreen by mutableStateOf(false)
        private set

    var showGroupInfoButton by mutableStateOf(false)
        private set

    var onGroupInfoClick by mutableStateOf<(() -> Unit)?>(null)
        private set

    fun setTitle(title: String, isMessages: Boolean) {
        topBarTitle = title
        isMessagesScreen = isMessages
    }

    fun setGroupInfoButton(show: Boolean, onClick: (() -> Unit)? = null) {
        showGroupInfoButton = show
        onGroupInfoClick = onClick
    }
}