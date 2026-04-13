package com.example.phinui.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phinui.data.schedule.ScheduleClass
import com.example.phinui.data.schedule.ScheduleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScheduleViewModel(
    private val repository: ScheduleRepository = ScheduleRepository()
) : ViewModel() {

    private val _classes = MutableStateFlow<List<ScheduleClass>>(emptyList())
    val classes: StateFlow<List<ScheduleClass>> = _classes.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadClasses()
    }

    fun loadClasses() {
        viewModelScope.launch {
            try {
                _classes.value = repository.getClasses()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun addClass(scheduleClass: ScheduleClass, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isSaving.value = true
            _error.value = null

            try {
                repository.addClass(scheduleClass)
                _classes.value = repository.getClasses()
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isSaving.value = false
            }
        }
    }
}