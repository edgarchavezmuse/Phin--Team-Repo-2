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

class CalendarViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    //  Authorization + calendar data state
    // Restored automatically if process is recreated
    var googleAccessToken by mutableStateOf(
        savedStateHandle.get<String>("googleAccessToken")
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

        // Persist token so state survives process recreation
        savedStateHandle["googleAccessToken"] = token

        loadEventsForCurrentWeek()
    }


    // Refresh events when updated
    fun refreshEvents() {
        if (googleAccessToken == null) return
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
        errorMessage = null
        isLoadingEvents = false
        eventsGroupedByDate = emptyMap()

        // Clear persisted state used by this ViewModel
        savedStateHandle["googleAccessToken"] = null
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

}