package com.example.phinui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.phinui.data.authorization.GoogleAuthManager
import com.example.phinui.data.calendar.CalendarEvent
import com.example.phinui.notifications.ExactAlarmPermissionRequest
import com.example.phinui.data.calendar.CalendarStorage
import com.example.phinui.viewmodel.CalendarViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import com.example.phinui.data.calendar.eventDate
import com.example.phinui.data.calendar.formatEventTimeLine
import com.example.phinui.data.calendar.formatReminderText
import com.example.phinui.data.calendar.CalendarSource


// Data formatters
private val weekRangeFormatter = DateTimeFormatter.ofPattern("MMM d")
private val dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEE")
private val selectedDateTitleFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    savedEvents: SnapshotStateList<CalendarEvent>,
    modifier: Modifier = Modifier,
    calendarViewModel: CalendarViewModel,
    onClick: (CalendarEvent) -> Unit,
    selectedEvent: MutableState<CalendarEvent?>,
    showRemoveDialog: MutableState<Boolean>
) {
    val context = LocalContext.current
    val activity = context as Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    // to trigger permission request if alarm permission is granted
    ExactAlarmPermissionRequest()
    // Variables for removing events from local calendar
    val coroutineScope = rememberCoroutineScope()
    val storage = CalendarStorage(context)

    //  Authorization + calendar data state
    val googleAccessToken = calendarViewModel.googleAccessToken
    val userEmail = calendarViewModel.userEmail
    val isLoadingEvents = calendarViewModel.isLoadingEvents
    val errorMessage = calendarViewModel.errorMessage
    val eventsGroupedByDate = calendarViewModel.eventsGroupedByDate

    //  Week navigation state
    val currentWeekStartDate = calendarViewModel.currentWeekStartDate

    // Builds the 7 day week
    val datesInCurrentWeek = calendarViewModel.datesInCurrentWeek
    val selectedDateInWeek = calendarViewModel.selectedDateInWeek

    // Merge Google events + local saved events for display only
    val googleEvents = eventsGroupedByDate.values.flatten()

    // Enforces rule: if not signed in don't show google calendar events from saved events
    val visibleSavedEvents = savedEvents.filter { it.source == CalendarSource.LOCAL }

    val mergedEvents = (googleEvents + visibleSavedEvents)
        .distinctBy { "${it.title.trim().lowercase()}-${it.start.take(16)}" }

    val displayedEventsGroupedByDate = groupEventsByDateForWeek(mergedEvents, currentWeekStartDate)

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

    /*
     * Auto-refresh calendar when screen is reopened
     * Uses ON_RESUME so it updates when navigating back or returning to the app.
     */
    DisposableEffect(lifecycleOwner, googleAccessToken) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && googleAccessToken != null) {
                calendarViewModel.refreshEvents()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
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
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            calendarViewModel.refreshEvents()
                        },
                        enabled = !isLoadingEvents
                    ) {
                        Text(if (isLoadingEvents) "Refreshing..." else "Refresh")
                    }

                    OutlinedButton(
                        onClick = {
                            val credentialManager = CredentialManager.create(activity)

                            coroutineScope.launch {
                                try {
                                    credentialManager.clearCredentialState(
                                        ClearCredentialStateRequest()
                                    )
                                } catch (_: Exception) {
                                }

                                calendarViewModel.signOut()
                            }
                        }
                    ) { Text("Sign Out") }
                }

                Text(
                    text = userEmail?.let { "Signed in as $it" } ?: "Signed in",
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
                            .clickable { onClick(event) }
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(Modifier.padding(14.dp)) {

                            val isGoogleEvent = event.source == CalendarSource.GOOGLE

                            // Title
                            Text(
                                text = event.title.ifBlank { "(No title)" },
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            val timeLine = formatEventTimeLine(event)
                            if (timeLine.isNotBlank()) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = timeLine,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Reminders
                            if (isGoogleEvent) {
                                val reminderText = formatReminderText(event.reminderMinutes)
                                if (reminderText != null) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "Reminders: $reminderText",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Location
                            event.location?.let { location ->
                                Spacer(Modifier.height(6.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Location",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = location,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // Removing local events from calendar
                if (showRemoveDialog.value && selectedEvent.value != null) {

                    // Check if Google Event
                    val eventToDelete = selectedEvent.value!!
                    val isGoogleEvent = eventToDelete.source == CalendarSource.GOOGLE
                    if (isGoogleEvent) {
                        AlertDialog(
                            onDismissRequest = { showRemoveDialog.value = false },
                            title = { Text("Remove Event") },
                            text = { Text("Would you like to remove \"${eventToDelete.title}\" from your Google Calendar?") },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            try {
                                                calendarViewModel.deleteGoogleEvent(eventToDelete)
                                                Toast.makeText(
                                                    context,
                                                    "${eventToDelete.title} removed from Google Calendar",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } catch (e: Exception) {
                                                Toast.makeText(
                                                    context,
                                                    e.message ?: "Failed to remove Google event.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } finally {
                                                showRemoveDialog.value = false
                                            }
                                        }
                                    }
                                ) {
                                    Text("Yes")
                                }
                            },
                            dismissButton = {
                                Button(
                                    onClick = { showRemoveDialog.value = false }
                                ) {
                                    Text("No")
                                }
                            }
                        )
                    }
                    else {
                        // Show dialog box for confirming the removal of the event
                        AlertDialog(
                            onDismissRequest = { showRemoveDialog.value = false },
                            title = { Text("Remove Event") },
                            text = { Text("Would you like to remove \"${eventToDelete.title}\" from your local calendar?") },

                            // If user taps 'yes' for removal of event
                            confirmButton = {
                                Button(onClick = {
                                    // Ensure the selected event is not null selectedEvent.value
                                    eventToDelete.let { event ->
                                        // Remove from CalendarStorage
                                        coroutineScope.launch(Dispatchers.IO) {
                                            storage.removeEvent(event)

                                            // Update calendar screen
                                            val updatedEvents = storage.loadEvents()
                                            withContext(Dispatchers.Main) {
                                                savedEvents.clear()
                                                savedEvents.addAll(updatedEvents)

                                                // Display confirmation message
                                                Toast.makeText(
                                                    context,
                                                    "${event.title} removed from calendar",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }

                                    // Close dialog box
                                    showRemoveDialog.value = false
                                }) {
                                    // Confirm button for removing event
                                    Text("Yes")
                                }
                            },
                            // Canceling the removal of event request
                            dismissButton = {
                                Button(onClick = { showRemoveDialog.value = false }) {
                                    Text("No")
                                }
                            }
                        )
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
