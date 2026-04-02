package com.example.phinui.data.calendar

import android.content.Context
import android.util.Log
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

        if (currentEvents.any { it.id == event.id }) {
            Log.d("CalendarStorage", "Event with id ${event.id} already exists. Skipping save.")
            return
        }

        val eventToSave = event.copy(
            source = event.source,
            googleEventID = event.googleEventID)
        currentEvents.add(eventToSave)
        saveAllEvents(currentEvents)

        Log.d("CalendarStorage", "Saving ${currentEvents.size} events")
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
                    // for linking google and local events
                    val googleEventID = obj.optString("googleEventID").ifBlank { null }
                    val source = if (googleEventID != null) CalendarSource.GOOGLE else CalendarSource.LOCAL

                    add(
                        CalendarEvent(
                            id = obj.optString("id"),
                            title = obj.optString("title"),
                            start = obj.optString("start"),
                            end = obj.optString("end"),
                            location = obj.optString("location").ifBlank { null },
                            description = obj.optString("description").ifBlank { null },
                            reminderMinutes = emptyList(),
                            source = source,
                            googleEventID = googleEventID
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
        Log.d("CalendarStorage", "Removing event with ID: ${event.id}")
        val currentEvents = loadEvents().toMutableList()

        Log.d("CalendarStorage", "Current events in storage before removal: ${currentEvents.map { it.id }}")
        val updatedEvents = currentEvents.filterNot { it.id == event.id }

        Log.d("CalendarStorage", "Remaining events Before remove: ${updatedEvents.map { it.id }}")
        saveAllEvents(updatedEvents)

        Log.d("CalendarStorage", "Remaining events after remove: ${updatedEvents.map { it.id }}")
    }

    suspend fun removeGoogleEvent(event: CalendarEvent) {
        Log.d("CalendarStorage", "Removing event with event.ID: ${event.id}")
        Log.d("CalendarStorage", "This is event.googleEventID: ${event.googleEventID}")

        val events = loadEvents().toMutableList()
        Log.d("CalendarStorage", "Current events in storage before removal: ${events.map { it.id }}")

        // Remove event either by local ID or by Google ID
        val remainingEvents = events.filterNot {
            Log.d("CalendarStorage", "This is it.googleEventID: ${it.googleEventID}")
            Log.d("CalendarStorage", "This is it.id: ${it.id}")
            (event.googleEventID != null && it.googleEventID == event.googleEventID) ||
            it.id == event.id
        }

        saveAllEvents(remainingEvents)

        Log.d("CalendarStorage", "Current events in storage after removal: ${events.map { it.id }}")
        Log.d("CalendarStorage", "Removed event with Google ID: ${event.googleEventID ?: "N/A"}")

        val localEvent = events.find { it.googleEventID == event.id }
        localEvent?.let { removeLocalEventByID(it.id) }
    }

    suspend fun removeLocalEventByID(localID: String) {
        Log.d("CalendarStorage", "In removeLocalEventByID")

        val currentEvents = loadEvents().toMutableList()
        val updatedEvents = currentEvents.filterNot { it.id == localID }
        saveAllEvents(updatedEvents)
        Log.d("CalendarStorage", "Removed local event with ID: $localID")
    }

    // for linking google and local events
    suspend fun updateEvent(updatedEvent: CalendarEvent) {
        val events = loadEvents().toMutableList()

        val index = events.indexOfFirst { it.id == updatedEvent.id}
        if (index != -1) {
            events[index] = updatedEvent
            saveAllEvents(events)
        }
    }

    // Save full list back into DataStore as JSON
    private suspend fun saveAllEvents(events: List<CalendarEvent>) {
        val jsonArray = JSONArray()

        if (events.isEmpty()) {
            Log.d("CalendarStorage", "No events to save")
        }
        else {
            Log.d("CalendarStorage", "Saving ${events.size} events")
        }
        events.forEach { event ->
            val obj = JSONObject().apply {
                put("id", event.id)
                put("title", event.title)
                put("start", event.start)
                put("end", event.end)
                put("location", event.location ?: "")
                put("description", event.description ?: "")
                put("googleEventID", event.googleEventID ?: "")
            }
            jsonArray.put(obj)
        }

        context.dataStore.edit { prefs ->
            prefs[EVENTS_KEY] = jsonArray.toString()
        }
    }
}