package com.example.phinui.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phinui.data.calendar.CalendarEvent
import com.example.phinui.data.calendar.GoogleCalendarRepository
import com.example.phinui.notifications.ReminderScheduler
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import com.example.phinui.data.calendar.CalendarSource
import com.example.phinui.data.calendar.CalendarStorage
import com.example.phinui.data.calendar.sameCalendarEvent
import java.util.UUID




sealed class AddEventResult {
    data class AddedToGoogle(val event: CalendarEvent, val googleEventID: String?) : AddEventResult()
    data object ShouldSaveLocally : AddEventResult()
}
class CalendarViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val reminderScheduler: ReminderScheduler,
    private val calendarStorage: CalendarStorage
) : ViewModel() {

    //  Authorization + calendar data state
    // Restored automatically if process is recreated
    var googleAccessToken by mutableStateOf(
        savedStateHandle.get<String>("googleAccessToken")
    )
        private set

    var userEmail by mutableStateOf(
        savedStateHandle.get<String>("userEmail")
    )
        private set

    var isLoadingEvents by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var eventsGroupedByDate by mutableStateOf<Map<LocalDate, List<CalendarEvent>>>(emptyMap())
        private set

    //private fun sameCalendarEvent(event1: CalendarEvent, event2: CalendarEvent): Boolean {
    //    return event1.id == event2.id
    //}

    //  Week navigation state
    // Restored from SavedStateHandle if available
    var currentReferenceDate by mutableStateOf(
        savedStateHandle.get<String>("currentReferenceDate")
            ?.let { LocalDate.parse(it) }
            ?: LocalDate.now()
    )
        private set

    val currentWeekStartDate: LocalDate
        get() = getStartOfWeek(currentReferenceDate)


    // Builds the 7 day week
    var selectedDateInWeek by mutableStateOf(
        savedStateHandle.get<String>("selectedDateInWeek")
            ?.let { LocalDate.parse(it) }
            ?: getStartOfWeek(LocalDate.now())
    )
        private set

    val datesInCurrentWeek: List<LocalDate>
        get() = (0..6).map { offset -> currentWeekStartDate.plusDays(offset.toLong()) }


    init {
        // If a token already exists (restored from state), reload events
        googleAccessToken?.let {
            loadEventsForCurrentWeek()
        }
    }

    // Load current events for the week the authorization is successful
    fun onAuthorizationSuccess(token: String) {
        googleAccessToken = token
        savedStateHandle["googleAccessToken"] = token

        viewModelScope.launch {
            val email = fetchUserEmail(token)
            userEmail = email
            savedStateHandle["userEmail"] = email

            // Sync local events to google
            try {
                val localEvents = calendarStorage.loadEvents()
                val googleEvents = eventsGroupedByDate.values.flatten()

                localEvents.forEach { localEvent ->
                    // Check if event has a google event ID.
                    val alreadyInGoogle = googleEvents.any { sameCalendarEvent(it, localEvent)}
                    if (!alreadyInGoogle) {
                        //if (localEvent.googleEventID == null) {
                        val addLocalEventToGoogleResult = addEventToAppropriateCalendar(localEvent)
                        if (addLocalEventToGoogleResult is AddEventResult.AddedToGoogle) {
                            // Update local event with google event ID
                            val updatedEvent = localEvent.copy(googleEventID = addLocalEventToGoogleResult.googleEventID)
                            calendarStorage.updateEvent(updatedEvent)
                        }
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Failed to sync local events: ${e.message}"
            }
            // End of syncing local events to google

            loadEventsForCurrentWeek()
        }
    }


    // Refresh events when updated
    fun refreshEvents() {
        if (googleAccessToken == null || isLoadingEvents) return
        loadEventsForCurrentWeek()
    }


    fun goToPreviousWeek() {
        currentReferenceDate = currentReferenceDate.minusDays(7)
        selectedDateInWeek = selectedDateInWeek.minusDays(7)

        persistWeekState()

        if (googleAccessToken != null) {
            loadEventsForCurrentWeek()
        }
    }


    fun goToNextWeek() {
        currentReferenceDate = currentReferenceDate.plusDays(7)
        selectedDateInWeek = selectedDateInWeek.plusDays(7)

        persistWeekState()

        if (googleAccessToken != null) {
            loadEventsForCurrentWeek()
        }
    }


    fun selectDate(date: LocalDate) {
        selectedDateInWeek = date

        // Persist selected date so it restores after process death
        savedStateHandle["selectedDateInWeek"] = date.toString()
    }


    fun setError(message: String?) {
        errorMessage = message
    }

    // Sign out of Google Calendar for this session
    fun signOut() {
        googleAccessToken = null
        userEmail = null
        errorMessage = null
        isLoadingEvents = false
        //eventsGroupedByDate = emptyMap()

        // ADDED 3/31
        viewModelScope.launch {
            val localEvents = calendarStorage.loadEvents()
            Log.d("CalendarStorage", "Local events BEFORE sign out: ${localEvents.map { it.id to it.googleEventID }}")
        }
        //END OF ADDED

        // Clear persisted state used by this ViewModel
        savedStateHandle["googleAccessToken"] = null
        savedStateHandle["userEmail"] = null

        // Load local events after sign out
        viewModelScope.launch {
            val localEvents = calendarStorage.loadEvents()

            //ADDED 3/31
            Log.d("CalendarStorage", "Local events after sign out: ${localEvents.map { it.id to it.googleEventID }}")
            //END OF ADDED

            eventsGroupedByDate = groupEventsByDateForWeek(localEvents, currentWeekStartDate)

            // ADDED 3/31
            Log.d(
                "CalendarStorage",
                "eventsGroupedByDate AFTER sign out: ${
                    eventsGroupedByDate.mapValues { entry -> entry.value.map { it.id } }
                }"
            )
            Log.d("CalendarStorage", "Local event dates: ${localEvents.map { it.start }}")
            Log.d("CalendarStorage", "Current week start: $currentWeekStartDate")
            // END OF ADDED
        }
    }

    fun goToCurrentWeek() {
        val today = LocalDate.now()
        currentReferenceDate = today
        selectedDateInWeek = today
        persistWeekState()

        if (googleAccessToken != null) {
            loadEventsForCurrentWeek()
        }
    }

    // Adds event to Google or local calendar depending on sign-in state
    suspend fun addEventToAppropriateCalendar(event: CalendarEvent): AddEventResult {
        val isSignedInToGoogle = googleAccessToken != null

        // Event is from event screen
        if (event.googleEventID == null) {
            // Check if event exists locally
            val localEvents = calendarStorage.loadEvents()

            //val existsLocally = localEvents.any { sameCalendarEvent(it, event) }

            // ADDED 3/31
            val existsLocally = localEvents.any {
                (it.googleEventID != null && it.googleEventID == event.googleEventID) ||
                sameCalendarEvent(it, event)
            }
            // END OF ADDED

            if (!existsLocally) {
                try {
                    withContext(Dispatchers.IO) {
                        calendarStorage.saveEvent(event) // Save to local storage
                    }
                    Log.d("CalendarStorage", "Saved event from event screen locally: $event")
                } catch (e: Exception) {
                    throw Exception("Failed to save event from event screen locally: ${e.message}")
                }
            }
        }

        // If signed in to Google and event doesn't exist in Google Calendar
        if (isSignedInToGoogle) {
            val googleEvents = GoogleCalendarRepository.fetchWeekEvents(googleAccessToken!!, LocalDate.now())

            //val existsInGoogle = googleEvents.any { sameCalendarEvent(it, event) }

            // ADDED 3/31
            val existsInGoogle = googleEvents.any {
                (event.googleEventID !=null && it.id == event.googleEventID) ||
                sameCalendarEvent(it, event)
            }
            // END OF ADDED

            if (!existsInGoogle) {
                try {
                    // Save to Google Calendar
                    val createdEvent = GoogleCalendarRepository.insertEvent(
                        accessToken = googleAccessToken!!,
                        event = event,
                        zone = ZoneId.systemDefault()
                    )

                    Log.d("CalendarStorage", "Event added to Google Calendar: $event")
                    //return AddEventResult.AddedToGoogle(event, googleEventID = null)

                    //ADDED 3/31
                    val updatedLocalEvent = event.copy(googleEventID = createdEvent.id) // <-- store Google ID
                    calendarStorage.updateEvent(updatedLocalEvent)

                    return AddEventResult.AddedToGoogle(createdEvent, createdEvent.googleEventID)
                    //END OF ADDED

                } catch (e: Exception) {
                    throw Exception("Failed to add event to Google Calendar: ${e.message}")
                }
            }
        }

        // If not signed in to Google, only save the event locally
        return AddEventResult.ShouldSaveLocally

        /* Old code
        //val token = googleAccessToken ?: return AddEventResult.ShouldSaveLocally

        //val createdEvent = GoogleCalendarRepository.insertEvent(
        //    accessToken = token,
        //    event = event,
        //    zone = ZoneId.systemDefault()
        //)

        val isSignedInToGoogle = googleAccessToken != null

        // Check if event exists in Google Calendar if signed in
        //val googleEvents = eventsGroupedByDate.values.flatten()
        val googleEvents = if (isSignedInToGoogle) {
            val weekStart = LocalDate.now().with(java.time.DayOfWeek.MONDAY)
            GoogleCalendarRepository.fetchWeekEvents(googleAccessToken!!, weekStart)
        } else {
            emptyList()
        }
        val existsInGoogle = googleEvents.any { sameCalendarEvent(it, event) }

        // Add event to google calendar if it doesn't exist
        if (isSignedInToGoogle && !existsInGoogle) {
            try {
                //val googleEvent = GoogleCalendarRepository.insertEvent(
                GoogleCalendarRepository.insertEvent(
                    accessToken = googleAccessToken!!,
                    event = event,
                    zone = ZoneId.systemDefault()
                )

                // Refresh events for current week
                loadEventsForCurrentWeek()
                return AddEventResult.AddedToGoogle(event, googleEventID = null)
                //return AddEventResult.AddedToGoogle(event, googleEvent.id)
            } catch (e: Exception) {
                throw Exception("Failed to add event to Google Calendar: ${e.message}")
            }
        }

        // If not signed in or already exists in Google, save it locally
        val localEvents = calendarStorage.loadEvents()
        val existsLocally = localEvents.any { sameCalendarEvent(it, event) }
        if (!existsLocally) {
            try {
                withContext(Dispatchers.IO) {
                    calendarStorage.saveEvent(event)
                }
                // Refresh events for current week
                //loadEventsForCurrentWeek()
                //return AddEventResult.ShouldSaveLocally

            } catch (e: Exception) {
                throw Exception("Failed to save event locally: ${e.message}")
            }
        }

        // Refresh events for current week
        loadEventsForCurrentWeek()
        return AddEventResult.ShouldSaveLocally

        end of old code */
    }

    /*
    * Data loader helper
    * Fetches events for a specific week and stores them grouped by LocalDate
    */
    private fun loadEventsForCurrentWeek() {

        val token = googleAccessToken ?: return


        isLoadingEvents = true
        errorMessage = null

        viewModelScope.launch {

            try {

                // Call repository to fetch events from Google Calendar API for the given week
                val events = GoogleCalendarRepository.fetchWeekEvents(
                    accessToken = token,
                    weekStart = currentWeekStartDate,
                    zone = ZoneId.systemDefault()
                )

                // store previous events to track removed ones
                val oldEventIds = eventsGroupedByDate.values.flatten().map { it.id }.toSet()

                eventsGroupedByDate = groupEventsByDateForWeek(
                    events = events,
                    weekStartDate = currentWeekStartDate
                )

                // schedule reminders for each event
                val newEvents = eventsGroupedByDate.values.flatten()
                val newEventIds = newEvents.map { it.id }.toSet()

                newEvents.forEach { event ->
                    reminderScheduler.scheduleReminder(event)
                }

                // cancel reminders for removed events
                (oldEventIds - newEventIds).forEach { removedId ->
                    reminderScheduler.cancelReminder(removedId)
                }

            } catch (e: Exception) {

                errorMessage = e.message ?: "Failed to load events."
                eventsGroupedByDate = emptyMap()

            } finally {

                isLoadingEvents = false

            }

        }

    }


    /*
    * Persist week navigation state
    * This ensures week + selected date survive screen recreation
    */
    private fun persistWeekState() {

        savedStateHandle["currentReferenceDate"] = currentReferenceDate.toString()
        savedStateHandle["selectedDateInWeek"] = selectedDateInWeek.toString()

    }

    // Fetches the users email
    private suspend fun fetchUserEmail(accessToken: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("https://www.googleapis.com/oauth2/v2/userinfo")
                val connection = url.openConnection() as HttpURLConnection

                connection.requestMethod = "GET"
                connection.setRequestProperty("Authorization", "Bearer $accessToken")
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                json.optString("email", null)

            } catch (_: Exception) {
                null
            }
        }
    }

    // Returns the Monday for the week
    private fun getStartOfWeek(
        date: LocalDate,
        weekStartsOn: DayOfWeek = DayOfWeek.MONDAY
    ): LocalDate {

        var currentDate = date

        while (currentDate.dayOfWeek != weekStartsOn) {
            currentDate = currentDate.minusDays(1)
        }

        return currentDate

    }


    // Groups events into the 7 days
    private fun groupEventsByDateForWeek(
        events: List<CalendarEvent>,
        weekStartDate: LocalDate
    ): Map<LocalDate, List<CalendarEvent>> {

        // Prepare 7 keys (one per day in the week), each with a mutable list
        val weekDates = (0..6).map { offset -> weekStartDate.plusDays(offset.toLong()) }

        val grouped = weekDates
            .associateWith { mutableListOf<CalendarEvent>() }
            .toMutableMap()

        // Put each event into the correct day bucket (if it falls within this week)
        for (event in events) {

            val eventStartDate = extractEventStartDate(event) ?: continue

            if (eventStartDate in grouped.keys) {
                grouped[eventStartDate]?.add(event)
            }

        }

        // Sort each day’s events by start time
        grouped.values.forEach { dayEvents ->
            dayEvents.sortBy { it.start }
        }

        return grouped.mapValues { it.value.toList() }

    }


    //  Extract just the LocalDate from event.start.
    private fun extractEventStartDate(event: CalendarEvent): LocalDate? {

        val startString = event.start

        if (startString.isBlank()) return null

        return try {

            if (!startString.contains('T')) {

                LocalDate.parse(startString)

            } else {

                // event format: YYYY-MM-DD
                LocalDate.parse(startString.substring(0, 10))

            }

        } catch (_: Exception) {

            null

        }

    }

    // Deletes google event
    suspend fun deleteGoogleEvent(event: CalendarEvent) {
        val token = googleAccessToken ?: throw IllegalStateException("Not signed in to Google Calendar.")

        if (event.source != CalendarSource.GOOGLE) {
            throw IllegalArgumentException("Only Google events can be deleted here.")
        }

        if (event.id.isBlank()) {
            throw IllegalArgumentException("Missing Google event ID.")
        }

        GoogleCalendarRepository.deleteEvent(
            accessToken = token,
            eventId = event.id
        )

        // ADDED 3/31
        calendarStorage.removeGoogleEvent(event)

        //calendarStorage.removeEvent(event)

        //val localEvents = calendarStorage.loadEvents()
        //val eventInLocalStorage = localEvents.find { it.id == event.id }
        //if (eventInLocalStorage != null) {
        //    calendarStorage.removeEvent(eventInLocalStorage)
        //}

        loadEventsForCurrentWeek()
    }

}