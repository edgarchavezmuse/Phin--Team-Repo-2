package com.example.phinui.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.phinui.data.schedule.CourseCatalogItem
import com.example.phinui.data.schedule.CourseCatalogRepository
import com.example.phinui.data.schedule.ScheduleClass
import com.example.phinui.data.schedule.ScheduleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScheduleViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = ScheduleRepository()
    private val courseCatalogRepository = CourseCatalogRepository()

    private val _classes = MutableStateFlow<List<ScheduleClass>>(emptyList())
    val classes: StateFlow<List<ScheduleClass>> = _classes.asStateFlow()

    private val _catalogCourses = MutableStateFlow<List<CourseCatalogItem>>(emptyList())
    val catalogCourses: StateFlow<List<CourseCatalogItem>> = _catalogCourses.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _isCatalogLoading = MutableStateFlow(false)
    val isCatalogLoading: StateFlow<Boolean> = _isCatalogLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadClasses()
        loadCourseCatalog()
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

    fun loadCourseCatalog() {
        viewModelScope.launch {
            _isCatalogLoading.value = true
            _error.value = null

            try {
                _catalogCourses.value =
                    courseCatalogRepository.fetchCourseCatalog(getApplication())
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isCatalogLoading.value = false
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

    fun searchCourses(query: String): List<CourseCatalogItem> {
        val trimmedQuery = query.trim().lowercase()

        if (trimmedQuery.isBlank()) return emptyList()

        return _catalogCourses.value
            .filter { course ->
                course.code.lowercase().contains(trimmedQuery) ||
                        course.name.lowercase().contains(trimmedQuery)
            }
            .take(15)
    }

    fun deleteClass(scheduleClass: ScheduleClass, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isSaving.value = true
            _error.value = null

            try {
                repository.deleteClass(scheduleClass)
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