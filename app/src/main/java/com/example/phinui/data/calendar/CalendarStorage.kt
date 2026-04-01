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

        //val localEvent = event.copy(source = CalendarSource.LOCAL)

        //currentEvents.add(localEvent)

        //ADDED 3/31
        val eventToSave = event.copy(
            source = event.source,
            googleEventID = event.googleEventID)
        currentEvents.add(eventToSave)
        //END OF ADDED

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

                    //ADDED 3/31
                    val source = if (googleEventID != null) CalendarSource.GOOGLE else CalendarSource.LOCAL
                    //END OF ADDED

                    add(
                        CalendarEvent(
                            id = obj.optString("id"),
                            title = obj.optString("title"),
                            start = obj.optString("start"),
                            end = obj.optString("end"),
                            location = obj.optString("location").ifBlank { null },
                            description = obj.optString("description").ifBlank { null },
                            reminderMinutes = emptyList(),
                            //source = CalendarSource.LOCAL,

                            //ADDED 3/31
                            source = source,
                            //END ADDED
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
        Log.d("CalendarStorage", "Inside of removeEvent")

        Log.d("CalendarStorage", "Removing event with ID: ${event.id}")

        //val currentEvents = loadEvents()
        val currentEvents = loadEvents().toMutableList()
        Log.d("CalendarStorage", "Current events in storage before removal: ${currentEvents.map { it.id }}")

        //Log.d("CalendarStorage", "Before Remove: ${currentEvents.size} events")
        //currentEvents.forEach {
        //   Log.d("CalendarStorage", "Event in storage: $it")
        //}

        // THIS ONE - will make it where local events don't all get removed
        // BUT - if you're signed in to google, it won't delete the local event too
        //val updatedEvents = loadEvents().filterNot { it.id == event.id }


        // ADDED 03/31
        val updatedEvents = currentEvents.filterNot { it.id == event.id }


        // THIS ONE - will delete both google and local event
        // BUT - it will remove all local events

        //val updatedEvents = currentEvents.filterNot {
        //    it.id == event.id || it.googleEventID == event.id || it.googleEventID == event.googleEventID }

        Log.d("CalendarStorage", "Remaining events Before remove: ${updatedEvents.map { it.id }}")

        //Log.d("CalendarStorage", "After Remove: ${currentEvents.size} events")
        //updatedEvents.forEach {
        //    Log.d("CalendarStorage", "Remaining event: $it")
        //}

        //if (updatedEvents.size != currentEvents.size) {
        //    saveAllEvents(updatedEvents)
        //} else {
        //    Log.d("CalendarStorage", "No events to save")
        //}

        //if (updatedEvents.isNotEmpty()) {
        //    saveAllEvents(updatedEvents)
        //} else {
        //    Log.d("CalendarStorage", "No events to save after removal")
        //}

        saveAllEvents(updatedEvents)

        Log.d("CalendarStorage", "Remaining events after remove: ${updatedEvents.map { it.id }}")

        //Log.d("CalendarStorage", "After Remove: ${currentEvents.size} events")
        //updatedEvents.forEach {
        //    Log.d("CalendarStorage", "Remaining event: $it")
        //}

        //if (updatedEvents.size != currentEvents.size) {
        //    saveAllEvents(updatedEvents)
        //} else {
        //    Log.d("CalendarStorage", "No events to save")
        //}

        //if (updatedEvents.isNotEmpty()) {
        //    saveAllEvents(updatedEvents)
        //} else {
        //    Log.d("CalendarStorage", "No events to save after removal")
        //}

        //saveAllEvents(updatedEvents)

        //currentEvents.forEach {
        //    Log.d("CalendarStorage", "id=${it.id}, googleId=${it.googleEventID}")
        Log.d("CalendarStorage", "Finished removeEvent")

    }

    suspend fun removeGoogleEvent(event: CalendarEvent) {

        /* START OF 2ND VERSION
        // ADDED 3/31 - SECOND VERSION
        Log.d("CalendarStorage", "Inside of removeGoogleEvent")
        Log.d("CalendarStorage", "Removing event with ID (Google): ${event.id}")
        Log.d("CalendarStorage", "This is event.googleEventID: ${event.googleEventID}")

        // Delete from Google Calendar first
        googleAccessToken?.let { token ->
            GoogleCalendarRepository.deleteEvent(accessToken = token, eventId = event.id)
            Log.d("CalendarStorage", "Deleted event from Google Calendar: ${event.id}")
        }

        val events = loadEvents().toMutableList()
        Log.d("CalendarStorage", "Current events in storage before removal: ${events.map { it.id }}")

        // Find the local event whose googleEventID matches event.id
        val localEvent = events.find { it.googleEventID == event.id }

        val remainingEvents = if (localEvent != null) {
            Log.d("CalendarStorage", "Found local event to remove: it.id = ${localEvent.id}, it.googleEventID = ${localEvent.googleEventID}")
            events.filterNot { it.id == localEvent.id } // remove by local ID
        } else {
            Log.d("CalendarStorage", "No matching local event found for Google ID: ${event.id}")
            events
        }

        saveAllEvents(remainingEvents)
        Log.d("CalendarStorage", "Saved ${remainingEvents.size} events after removal")

        // Optional: remove via removeEvent for consistency
        localEvent?.let {
            removeEvent(it)
            Log.d("CalendarStorage", "Removed local event via removeEvent: ${it.id}")
        }

        Log.d("CalendarStorage", "Finished removeGoogleEvent")
        // END OF SECOND VERSION
        END OF 2ND VERSION
         */

        ///* OLD FIRST VERSION
        //ADDED 3/31
        Log.d("CalendarStorage", "Inside of removeGoogleEvent")
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

        Log.d("CalendarStorage", "Finished removeGoogleEvent")

        // ADDED AT 10:30PM
        //val localEvent = events.find {it.id == event.id}
        //localEvent?.let { removeEvent(it) }

        //END OF ADDED AT 10:30PM
        val localEvent = events.find { it.googleEventID == event.id }
        localEvent?.let { removeLocalEventByID(it.id) }
        //ADDED AT 10:53PM

        //END OF ADDED AT 10:53PM

        //END OF ADDED
        //END OF 1ST VERSION
        // */

        /* START OF OLD CODE
        Log.d("CalendarStorage", "Inside of removeGoogleEvent")
        Log.d("CalendarStorage", "Removing event with ID: ${event.id}")

        //val currentEvents = loadEvents()
        val currentEvents = loadEvents().toMutableList()
        //Log.d("CalendarStorage", "Current events in storage before removal: ${currentEvents.map { it.id }}")

        // ADDED 03/31

            // aggressive but removes all
        //val updatedEvents = currentEvents.filterNot { it.id == event.id || it.googleEventID == event.id || it.googleEventID == event.googleEventID }

            // removes from google calendar but stays on screen
        //val updatedEvents = currentEvents.filterNot { it.id == event.id || it.googleEventID == event.id}

        //val updatedEvents = currentEvents.filterNot { it.id == event.id || it.googleEventID == event.googleEventID }
        //val updatedEvents = currentEvents.filterNot { it.googleEventID == event.id || it.googleEventID == event.googleEventID }
        //val updatedEvents = currentEvents.filterNot { it.googleEventID == event.googleEventID }

            // THIS IS THE ONE THAT DELETES JUST THE GOOGLE EVENT!!
        val updatedEvents = currentEvents.filterNot { it.googleEventID == event.id }

            // this one errored out
        //val updatedEvents = currentEvents.filterNot { it.id == event.id }

        //Log.d("CalendarStorage", "Remaining events after remove: ${updatedEvents.map { it.id }}")

        saveAllEvents(updatedEvents)

        //Log.d("CalendarStorage", "Remaining events after remove: ${updatedEvents.map { it.id }}")

        Log.d("CalendarStorage", "Finished removeGoogleEvent")

        END OF OLD CODE

         */

    }

    //ADDED 3/31
    suspend fun removeLocalEventByID(localID: String) {
        Log.d("CalendarStorage", "In removeLocalEventByID")

        val currentEvents = loadEvents().toMutableList()
        val updatedEvents = currentEvents.filterNot { it.id == localID }
        saveAllEvents(updatedEvents)
        Log.d("CalendarStorage", "Removed local event with ID: $localID")
    }
    //END OF ADDED

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