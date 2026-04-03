package com.example.phinui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phinui.data.calendar.CalendarEvent
import com.example.phinui.data.events.RetrofitInstance
import com.example.phinui.data.events.toCalendarEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class EventsRepository {
    suspend fun fetchEvents(): List<CalendarEvent> {
        val rssItems = RetrofitInstance.api.getEvents().channel?.items ?: emptyList()

        val now = LocalDateTime.now()

        return rssItems
            .map { it.toCalendarEvent() }
            // filter for only upcoming events
            .filter { event ->
                val start = event.start.toLocalDateTimeOrNull()
                val end = event.end.toLocalDateTimeOrNull()

                start != null && end != null && end.isAfter(now)
            }
            .sortedBy { it.start.toLocalDateTimeOrNull() }
            .take(20)
    }
}

fun String.toLocalDateTimeOrNull(): LocalDateTime? {
    return try {
        LocalDateTime.parse(this)
    } catch (e: Exception) {
        null
    }
}

class EventsViewModel(private val repository: EventsRepository) : ViewModel() {
    private val _events = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val events: StateFlow<List<CalendarEvent>> = _events

    init {
        fetchEvents()
    }

    private fun fetchEvents() {
        viewModelScope.launch {
            try {
                val eventsList = repository.fetchEvents()
                Log.d("EventsVM", "Fetched ${eventsList.size} events")
                _events.value = eventsList
            } catch (e: Exception) {
                //_events.value = emptyList()
                Log.e("EventsVM", "Failed to fetch events", e)
            }
        }
    }
}