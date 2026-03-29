package com.example.phinui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.phinui.notifications.ReminderScheduler
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.phinui.data.calendar.CalendarStorage

// in order to allow ReminderScheduler parameter
class CalendarViewModelFactory(
    private val reminderScheduler: ReminderScheduler,
    private val calendarStorage: CalendarStorage
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras
    ) : T {
        if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
            // CreationExtras API to get SavedStateHandle
            val savedStateHandle: SavedStateHandle = extras.createSavedStateHandle()
            return CalendarViewModel(savedStateHandle, reminderScheduler, calendarStorage) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}