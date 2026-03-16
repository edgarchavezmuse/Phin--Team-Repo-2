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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.phinui.data.authorization.GoogleAuthManager
import com.example.phinui.data.calendar.CalendarEvent
import com.example.phinui.viewmodel.CalendarViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter


// Data formatters
private val weekRangeFormatter = DateTimeFormatter.ofPattern("MMM d")
private val dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEE")
private val selectedDateTitleFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    savedEvents: List<CalendarEvent>,
    modifier: Modifier = Modifier,
    calendarViewModel: CalendarViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as Activity

    //  Authorization + calendar data state
    val googleAccessToken = calendarViewModel.googleAccessToken
    val isLoadingEvents = calendarViewModel.isLoadingEvents
    val errorMessage = calendarViewModel.errorMessage
    val eventsGroupedByDate = calendarViewModel.eventsGroupedByDate

    //  Week navigation state
    val currentWeekStartDate = calendarViewModel.currentWeekStartDate

    // Builds the 7 day week
    val datesInCurrentWeek = calendarViewModel.datesInCurrentWeek
    val selectedDateInWeek = calendarViewModel.selectedDateInWeek

    // Merge Google events + local saved events for display only
    val displayedEventsGroupedByDate = remember(
        eventsGroupedByDate,
        savedEvents,
        currentWeekStartDate
    ) {
        val googleEvents = eventsGroupedByDate.values.flatten()
        val mergedEvents = (googleEvents + savedEvents).distinctBy { it.id }
        groupEventsByDateForWeek(mergedEvents, currentWeekStartDate)
    }

    //  Authorization launcher (opens Google consent UI)
    val authorizationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        val tokenFromResult = GoogleAuthManager.handleAuthorizationResult(activity, result)
        if (tokenFromResult.isNullOrBlank()) {
            calendarViewModel.setError("Authorization canceled or failed.")
            return@rememberLauncherForActivityResult
        }

        // Save token & load this weeks events
        calendarViewModel.onAuthorizationSuccess(tokenFromResult)
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
                onClick = { calendarViewModel.goToPreviousWeek() }
            ) { Text("Prev") }

            // Move displayed week forward by 7 days
            TextButton(
                onClick = { calendarViewModel.goToNextWeek() }
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
                        calendarViewModel.setError(null)

                        GoogleAuthManager.startAuthorization(
                            activity = activity,
                            launcher = authorizationLauncher,
                            onAccessToken = { immediateToken ->
                                calendarViewModel.onAuthorizationSuccess(immediateToken)
                            },
                            onError = { exception ->
                                calendarViewModel.setError(
                                    exception.message ?: "Authorization error."
                                )
                            }
                        )
                    }
                ) { Text("Connect Google Calendar") }
            } else {
                // Manual Reload events
                OutlinedButton(
                    onClick = {
                        calendarViewModel.refreshEvents()
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
                    onClick = { calendarViewModel.selectDate(date) },
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
        val eventsForSelectedDate = displayedEventsGroupedByDate[selectedDateInWeek].orEmpty()

        // Decide what to show based on auth/loading/data state
        when {
            googleAccessToken == null && eventsForSelectedDate.isEmpty() -> {
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

                            // Display location of event
                            event.location?.let {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall,
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

private fun groupEventsByDateForWeek(
    events: List<CalendarEvent>,
    weekStartDate: LocalDate
): Map<LocalDate, List<CalendarEvent>> {
    val weekDates = (0L..6L).map { weekStartDate.plusDays(it) }

    return weekDates.associateWith { date ->
        events.filter { event ->
            eventDate(event) == date
        }
    }
}

private fun eventDate(event: CalendarEvent): LocalDate? {
    val start = event.start
    if (start.isBlank()) return null

    return try {
        if (start.contains("T")) {
            LocalDate.parse(start.substring(0, 10))
        } else {
            LocalDate.parse(start)
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