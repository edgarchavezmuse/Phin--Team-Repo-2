package com.example.phinui.data.calendar

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject


val Context.dataStore by preferencesDataStore(name = "calendar")

// this is to make storage persistent
class CalendarStorage(private val context: Context) {

    private val EVENTS_KEY = stringPreferencesKey("saved_events")

    // saves the event in "dataStore"
    suspend fun saveEvent(event: CalendarEvent) {
        val currentEvents = loadEvents().toMutableList()

        val localEvent = event.copy(source = CalendarSource.LOCAL)

        currentEvents.add(localEvent)
        saveAllEvents(currentEvents)
    }

    // loads events from "dataStore"
    suspend fun loadEvents(): List<CalendarEvent> {
        val prefs = context.dataStore.data.first()
        val saved = prefs[EVENTS_KEY] ?: ""

        if (saved.isBlank()) return emptyList()

        return try {
            val jsonArray = JSONArray(saved)

            buildList {
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)

                    add(
                        CalendarEvent(
                            id = obj.optString("id"),
                            title = obj.optString("title"),
                            start = obj.optString("start"),
                            end = obj.optString("end"),
                            location = obj.optString("location").ifBlank { null },
                            description = obj.optString("description").ifBlank { null },
                            reminderMinutes = emptyList(),
                            source = CalendarSource.LOCAL
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    // remove selected event from "dataStore"
    suspend fun removeEvent(event: CalendarEvent) {
        val updatedEvents = loadEvents().filterNot { it.id == event.id }
        saveAllEvents(updatedEvents)
    }
    // Save full list back into DataStore as JSON
    private suspend fun saveAllEvents(events: List<CalendarEvent>) {
        val jsonArray = JSONArray()

        events.forEach { event ->
            val obj = JSONObject().apply {
                put("id", event.id)
                put("title", event.title)
                put("start", event.start)
                put("end", event.end)
                put("location", event.location ?: "")
                put("description", event.description ?: "")
        }
        jsonArray.put(obj)
    }

    context.dataStore.edit { prefs ->
        prefs[EVENTS_KEY] = jsonArray.toString()
        }
    }
}