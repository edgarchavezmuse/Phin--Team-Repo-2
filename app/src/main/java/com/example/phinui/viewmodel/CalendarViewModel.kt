package com.example.phinui.viewmodel

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
import com.example.phinui.data.authorization.GoogleCalendarSessionStorage
import com.example.phinui.data.calendar.GoogleCalendarUnauthorizedException

sealed class AddEventResult {
    data class AddedToGoogle(val event: CalendarEvent) : AddEventResult()
    data object ShouldSaveLocally : AddEventResult()
}
class CalendarViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val reminderScheduler: ReminderScheduler,
    private val sessionStorage: GoogleCalendarSessionStorage
) : ViewModel() {

    //  Authorization + calendar data state
    // Restored automatically if process is recreated
    var googleAccessToken by mutableStateOf<String?>(null)
        private set

    val isGoogleCalendarConnected: Boolean
        get() = sessionStorage.isConnected()

    var isRestoringGoogleSession by mutableStateOf(false)
        private set

    var userEmail by mutableStateOf(
        savedStateHandle.get<String>("userEmail")
            ?: sessionStorage.getUserEmail()
    )
        private set

    var isLoadingEvents by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var eventsGroupedByDate by mutableStateOf<Map<LocalDate, List<CalendarEvent>>>(emptyMap())
        private set


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
            ?: LocalDate.now()
    )
        private set

    val datesInCurrentWeek: List<LocalDate>
        get() = (0..6).map { offset -> currentWeekStartDate.plusDays(offset.toLong()) }


    // Load current events for the week the authorization is successful
    fun onAuthorizationSuccess(token: String) {
        googleAccessToken = token
        isRestoringGoogleSession = false

        viewModelScope.launch {
            val email = fetchUserEmail(token)
            userEmail = email
            savedStateHandle["userEmail"] = email

            sessionStorage.saveSession(userEmail = email)

            loadEventsForCurrentWeek()
        }
    }

    fun beginGoogleSessionRestore() {
        if (!sessionStorage.isConnected()) return
        if (googleAccessToken != null) return
        if (isRestoringGoogleSession) return

        isRestoringGoogleSession = true
        errorMessage = null
    }

    fun onGoogleSessionRestoreFailed(message: String? = null) {
        isRestoringGoogleSession = false
        googleAccessToken = null
        errorMessage = message ?: "Failed to restore Google Calendar session."
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
        isRestoringGoogleSession = false
        eventsGroupedByDate = emptyMap()

        // Clear persisted state used by this ViewModel
        savedStateHandle["userEmail"] = null

        sessionStorage.clearSession()
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
        val token = googleAccessToken ?: return AddEventResult.ShouldSaveLocally

        return try {
            val createdEvent = GoogleCalendarRepository.insertEvent(
                accessToken = token,
                event = event,
                zone = ZoneId.systemDefault()
            )

            loadEventsForCurrentWeek()
            AddEventResult.AddedToGoogle(createdEvent)

        } catch (e: GoogleCalendarUnauthorizedException) {
            googleAccessToken = null
            isRestoringGoogleSession = false
            errorMessage = "Session expired. Please reconnect."
            AddEventResult.ShouldSaveLocally
        }
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
                    reminderScheduler.cancelGoogleCalendarReminder(removedId)
                }

            } catch (e: GoogleCalendarUnauthorizedException) {
                googleAccessToken = null
                isRestoringGoogleSession = false
                errorMessage = "Session expired. Reconnecting required."
                eventsGroupedByDate = emptyMap()

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
        val token = googleAccessToken
            ?: throw IllegalStateException("Not signed in to Google Calendar.")

        if (event.source != CalendarSource.GOOGLE) {
            throw IllegalArgumentException("Only Google events can be deleted here.")
        }

        if (event.id.isBlank()) {
            throw IllegalArgumentException("Missing Google event ID.")
        }

        try {
            GoogleCalendarRepository.deleteEvent(
                accessToken = token,
                eventId = event.id
            )

            loadEventsForCurrentWeek()

        } catch (e: GoogleCalendarUnauthorizedException) {
        googleAccessToken = null
        isRestoringGoogleSession = false
        errorMessage = "Session expired. Please reconnect."
        throw e
    }
    }
}