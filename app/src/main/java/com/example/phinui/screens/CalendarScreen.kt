package com.example.phinui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.phinui.data.authorization.GoogleAuthManager
import com.example.phinui.data.calendar.CalendarEvent
import com.example.phinui.data.calendar.GoogleCalendarRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter


// Data formatters
private val weekRangeFormatter = DateTimeFormatter.ofPattern("MMM d")
private val dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEE")
private val selectedDateTitleFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activity = context as Activity
    val coroutineScope = rememberCoroutineScope()

    //  Authorization + calendar data state
    var googleAccessToken by remember { mutableStateOf<String?>(null) }
    var isLoadingEvents by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var eventsGroupedByDate by remember { mutableStateOf<Map<LocalDate, List<CalendarEvent>>>(emptyMap()) }

    //  Week navigation state
    var currentReferenceDate by remember { mutableStateOf(LocalDate.now()) }
    val currentWeekStartDate = remember(currentReferenceDate) {
        getStartOfWeek(currentReferenceDate, weekStartsOn = DayOfWeek.MONDAY)
    }

    // Builds the 7 day week
    val datesInCurrentWeek = remember(currentWeekStartDate) {
        (0..6).map { offset -> currentWeekStartDate.plusDays(offset.toLong()) }
    }
    var selectedDateInWeek by remember(currentWeekStartDate) {
        mutableStateOf(currentWeekStartDate)
    }

    //  Authorization launcher (opens Google consent UI)
    val authorizationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        val tokenFromResult = GoogleAuthManager.handleAuthorizationResult(activity, result)
        if (tokenFromResult.isNullOrBlank()) {
            errorMessage = "Authorization canceled or failed."
            return@rememberLauncherForActivityResult
        }

        // Save token & load this weeks events
        googleAccessToken = tokenFromResult

        loadEventsForWeek(
            accessToken = tokenFromResult,
            weekStartDate = currentWeekStartDate,
            coroutineScope = coroutineScope,
            updateLoadingState = { isLoadingEvents = it },
            updateErrorState = { errorMessage = it },
            updateEventsGroupedByDate = { eventsGroupedByDate = it }
        )
    }

    // If the week changes and we already have a token, so reload automatically.
    LaunchedEffect(currentWeekStartDate, googleAccessToken) {
        val token = googleAccessToken ?: return@LaunchedEffect
        loadEventsForWeek(
            accessToken = token,
            weekStartDate = currentWeekStartDate,
            coroutineScope = coroutineScope,
            updateLoadingState = { isLoadingEvents = it },
            updateErrorState = { errorMessage = it },
            updateEventsGroupedByDate = { eventsGroupedByDate = it }
        )
    }

    // Main Calendar UI Layout
    Column(modifier.padding(16.dp)) {

        //  Header (week range + prev/next)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val weekEndDate = currentWeekStartDate.plusDays(6)
                Text(
                    text = "${currentWeekStartDate.format(weekRangeFormatter)} – ${weekEndDate.format(weekRangeFormatter)}",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Week view",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Move displayed week backward by 7 days
            TextButton(
                onClick = {
                    currentReferenceDate = currentReferenceDate.minusDays(7)
                    selectedDateInWeek = selectedDateInWeek.minusDays(7)
                }
            ) { Text("Prev") }

            // Move displayed week forward by 7 days
            TextButton(
                onClick = {
                    currentReferenceDate = currentReferenceDate.plusDays(7)
                    selectedDateInWeek = selectedDateInWeek.plusDays(7)
                }
            ) { Text("Next") }
        }

        Spacer(Modifier.height(12.dp))

        // Connect / Refresh row
        // - If not connected: show "Connect Google Calendar"
        // - If connected: show "Refresh" + status text
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (googleAccessToken == null) {
                Button(
                    onClick = {
                        errorMessage = null

                        GoogleAuthManager.startAuthorization(
                            activity = activity,
                            launcher = authorizationLauncher,
                            onAccessToken = { immediateToken ->
                                googleAccessToken = immediateToken

                                loadEventsForWeek(
                                    accessToken = immediateToken,
                                    weekStartDate = currentWeekStartDate,
                                    coroutineScope = coroutineScope,
                                    updateLoadingState = { isLoadingEvents = it },
                                    updateErrorState = { errorMessage = it },
                                    updateEventsGroupedByDate = { eventsGroupedByDate = it }
                                )
                            },
                            onError = { exception ->
                                errorMessage = exception.message ?: "Authorization error."
                            }
                        )
                    }
                ) { Text("Connect Google Calendar") }
            } else {
                // Manual Reload events
                OutlinedButton(
                    onClick = {
                        val token = googleAccessToken ?: return@OutlinedButton
                        loadEventsForWeek(
                            accessToken = token,
                            weekStartDate = currentWeekStartDate,
                            coroutineScope = coroutineScope,
                            updateLoadingState = { isLoadingEvents = it },
                            updateErrorState = { errorMessage = it },
                            updateEventsGroupedByDate = { eventsGroupedByDate = it }
                        )
                    }
                ) { Text("Refresh") }

                Text(
                    text = "Connected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Loading events + error states
        if (isLoadingEvents) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator()
                Spacer(Modifier.width(12.dp))
                Text("Loading events…")
            }
            Spacer(Modifier.height(12.dp))
        }

        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(12.dp))
        }

        HorizontalDivider()
        Spacer(Modifier.height(14.dp))

        // Scrollable Day chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            datesInCurrentWeek.forEach { date ->
                val isSelected = date == selectedDateInWeek

                Surface(
                    onClick = { selectedDateInWeek = date },
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surface,
                    tonalElevation = if (isSelected) 2.dp else 0.dp,
                    border = if (!isSelected) ButtonDefaults.outlinedButtonBorder() else null
                ) {
                    Column(
                        modifier = Modifier
                            .width(64.dp)
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = date.format(dayOfWeekFormatter),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Clip
                        )
                        Text(
                            text = date.dayOfMonth.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        //  Selected day title + event list
        Text(
            text = selectedDateInWeek.format(selectedDateTitleFormatter),
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(8.dp))

        // Pull the events for the selected day out of the grouped map
        val eventsForSelectedDate = eventsGroupedByDate[selectedDateInWeek].orEmpty()

        // Decide what to show based on auth/loading/data state
        when {
            googleAccessToken == null -> {
                Text(
                    text = "Connect your calendar to see events.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            !isLoadingEvents && eventsForSelectedDate.isEmpty() -> {
                Text(
                    text = "No events",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            else -> {
                Spacer(Modifier.height(8.dp))
                // Render each event in a card
                eventsForSelectedDate.forEach { event ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text(
                                text = event.title.ifBlank { "(No title)" },
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Format "HH:MM – HH:MM" or "All day" depending on event type
                            val timeLine = formatEventTimeLine(event)
                            if (timeLine.isNotBlank()) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = timeLine,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/*
 * Data loader helper
 * Fetches events for a specific week and stores them grouped by LocalDate
 */
private fun loadEventsForWeek(
    accessToken: String,
    weekStartDate: LocalDate,
    coroutineScope: CoroutineScope,
    updateLoadingState: (Boolean) -> Unit,
    updateErrorState: (String?) -> Unit,
    updateEventsGroupedByDate: (Map<LocalDate, List<CalendarEvent>>) -> Unit
) {
    updateLoadingState(true)
    updateErrorState(null)

    coroutineScope.launch {
        try {
            // Call repository to fetch events from Google Calendar API for the given week
            val events = GoogleCalendarRepository.fetchWeekEvents(
                accessToken = accessToken,
                weekStart = weekStartDate,
                zone = ZoneId.systemDefault()
            )

            val groupedEvents = groupEventsByDateForWeek(
                events = events,
                weekStartDate = weekStartDate
            )

            updateEventsGroupedByDate(groupedEvents)
        } catch (e: Exception) {
            updateErrorState(e.message ?: "Failed to load events.")
            updateEventsGroupedByDate(emptyMap())
        } finally {
            updateLoadingState(false)
        }
    }
}

// Returns the Monday for the week .
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
    val grouped = weekDates.associateWith { mutableListOf<CalendarEvent>() }.toMutableMap()

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
            LocalDate.parse(startString) // all-day event
        } else {
            // event format: YYYY-MM-DD
            LocalDate.parse(startString.substring(0, 10))
        }
    } catch (_: Exception) {
        null
    }
}

// Event display helper
private fun formatEventTimeLine(event: CalendarEvent): String {
    val startString = event.start
    val endString = event.end

    if (startString.isBlank()) return ""

    // All-day events have only "date" not time (no 'T')
    if (!startString.contains('T')) return "All day"

    // Pull out HH:MM from HH:MM:SS
    fun extractHourMinute(isoDateTime: String): String {
        if (!isoDateTime.contains('T') || isoDateTime.length < 16) return ""
        return isoDateTime.substring(11, 16) // "HH:MM"
    }

    val startTime = extractHourMinute(startString)
    val endTime = if (endString.isNotBlank()) extractHourMinute(endString) else ""

    return when {
        startTime.isNotBlank() && endTime.isNotBlank() -> "$startTime – $endTime"
        startTime.isNotBlank() -> startTime
        else -> ""
    }
}