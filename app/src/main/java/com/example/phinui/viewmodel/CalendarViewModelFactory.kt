package com.example.phinui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.phinui.notifications.ReminderScheduler
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import android.content.Context
import com.example.phinui.data.authorization.GoogleCalendarSessionStorage
import com.example.phinui.data.calendar.GoogleCalendarFirebaseRepository

class CalendarViewModelFactory(
    private val context: Context,
    private val reminderScheduler: ReminderScheduler,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras
    ): T {
        if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
            val savedStateHandle: SavedStateHandle = extras.createSavedStateHandle()
            val sessionStorage = GoogleCalendarSessionStorage(context.applicationContext)
            val firebaseRepository = GoogleCalendarFirebaseRepository()

            return CalendarViewModel(
                savedStateHandle = savedStateHandle,
                reminderScheduler = reminderScheduler,
                sessionStorage = sessionStorage,
                firebaseRepository = firebaseRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}