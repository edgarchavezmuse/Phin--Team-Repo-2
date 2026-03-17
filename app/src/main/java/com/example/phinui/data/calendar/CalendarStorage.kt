package com.example.phinui.data.calendar

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first


val Context.dataStore by preferencesDataStore(name = "calendar")

// this is to make storage persistent
class CalendarStorage(private val context: Context) {

    private val EVENTS_KEY = stringPreferencesKey("saved_events")

    // saves the event in "dataStore"
    suspend fun saveEvent(event: CalendarEvent) {
        context.dataStore.edit { prefs ->
            val current = prefs[EVENTS_KEY] ?: ""
            prefs[EVENTS_KEY] = current + "${event.id}|${event.title}|${event.start}|${event.end}|${event.location};"
        }
    }

    // loads events from "dataStore"
    suspend fun loadEvents(): List<CalendarEvent> {
        val prefs = context.dataStore.data.first()
        val saved = prefs[EVENTS_KEY] ?: ""

        return saved.split(";")
            .filter { it.contains("|") }
            .map {
                val parts = it.split("|")
                CalendarEvent(
                    id = parts.getOrNull(0) ?: "",
                    title = parts.getOrNull(1) ?: "",
                    start = parts.getOrNull(2) ?: "",
                    end = parts.getOrNull(3) ?: "",
                    location = parts.getOrNull(4)
                )
            }
    }

    // remove selected event from "dataStore"
    suspend fun removeEvent(event: CalendarEvent) {
        context.dataStore.edit { prefs ->
            val current = prefs[EVENTS_KEY] ?: ""
            val updated = current
                .split(";")
                .filter { it.contains("|") && it.isNotBlank() }
                .map {
                    val parts = it.split("|")
                    CalendarEvent(
                        id = parts.getOrNull(0) ?: "",
                        title = parts.getOrNull(1) ?: "",
                        start = parts.getOrNull(2) ?: "",
                        end = parts.getOrNull(3) ?: "",
                        location = parts.getOrNull(4)
                    )
                }
                // removes the selected event by ID number
                .filter { it.id != event.id }
                .joinToString(";") { "${it.id}|${it.title}|${it.start}|${it.end}|${it.location}" }
            prefs[EVENTS_KEY] = updated
        }
    }
}