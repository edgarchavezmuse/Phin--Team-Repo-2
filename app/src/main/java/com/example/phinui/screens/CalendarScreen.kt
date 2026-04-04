package com.example.phinui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.phinui.data.calendar.eventDate
import com.example.phinui.data.calendar.CalendarSource
import com.example.phinui.components.calendar.*
import com.example.phinui.ui.components.calendar.CalendarEmptyState
import com.example.phinui.ui.components.calendar.CalendarHeader
import com.example.phinui.ui.components.calendar.EventCard
import com.example.phinui.ui.components.calendar.WeekDateSelector
import com.example.phinui.ui.components.calendar.CalendarConnectionCard
import com.example.phinui.data.events.EventDetails


private val PrimaryRed = Color(0xFFE53935)
private val SoftBackground = Color(0xFFFFFBFA)
private val TextDark = Color(0xFF1F1F1F)
private val TextMuted = Color(0xFF666666)
private val selectedDateTitleFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    savedEvents: SnapshotStateList<CalendarEvent>,
    modifier: Modifier = Modifier,
    calendarViewModel: CalendarViewModel,
    onClick: (CalendarEvent) -> Unit,
    onConnectClick: () -> Unit,
    onAddEventClick: () -> Unit,
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
    val isGoogleCalendarConnected = calendarViewModel.isGoogleCalendarConnected

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
    Column(modifier = modifier.padding(16.dp)) {

        //  Header (week range + prev/next)
        CalendarHeader(
            currentWeekStartDate = currentWeekStartDate,
            onRefreshClick = { calendarViewModel.refreshEvents() },
            isRefreshing = isLoadingEvents,
            isSignedIn = isGoogleCalendarConnected
        )

        Spacer(Modifier.height(12.dp))

        // Connect / Refresh row
        // - If not connected: show "Connect Google Calendar"
        // - If connected: show "Refresh" + status text
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!isGoogleCalendarConnected && googleAccessToken == null) {
                CalendarConnectionCard(
                    onConnectClick = onConnectClick
                )
            } else {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when {
                            googleAccessToken != null && userEmail != null -> "Signed in as $userEmail"
                            googleAccessToken != null -> "Signed in"
                            else -> "Reconnecting to Google Calendar..."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )

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
                        },
                        enabled = isGoogleCalendarConnected,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Sign out")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        CalendarWeekNavigation(
            onPreviousWeek = { calendarViewModel.goToPreviousWeek() },
            onTodayClick = { calendarViewModel.goToCurrentWeek() },
            onNextWeek = { calendarViewModel.goToNextWeek() }
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onAddEventClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Event")
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
        WeekDateSelector(
            dates = datesInCurrentWeek,
            selectedDate = selectedDateInWeek,
            onSelect = { calendarViewModel.selectDate(it) }
        )

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
            // When no events print:
            !isGoogleCalendarConnected && eventsForSelectedDate.isEmpty() -> {
                CalendarEmptyState(
                    title = "Connect your calendar",
                    subtitle = "Sign in to see your events."
                )
            }

            isGoogleCalendarConnected && googleAccessToken == null && eventsForSelectedDate.isEmpty() -> {
                CalendarEmptyState(
                    title = "Reconnecting...",
                    subtitle = "Restoring your Google Calendar session."
                )
            }

            !isLoadingEvents && eventsForSelectedDate.isEmpty() -> {
                CalendarEmptyState(
                    title = "No events",
                    subtitle = "You're all clear for this day."
                )
            }

            // When events present:
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(eventsForSelectedDate) { event ->
                        EventCard(
                            event = event,
                            onClick = { onClick(event) }
                        )
                    }
                }
            }
        }
    }

    // Removing events from calendar
    if (showRemoveDialog.value && selectedEvent.value != null) {

        // Check if Google Event
        val eventToDelete = selectedEvent.value!!
        val isGoogleEvent = eventToDelete.source == CalendarSource.GOOGLE

        AlertDialog(
            onDismissRequest = { showRemoveDialog.value = false },
            containerColor = SoftBackground,
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = eventToDelete.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            },
            text = {
                EventDetails(event = eventToDelete)
            },

            // If user taps 'Remove from calendar' for removal of event
            confirmButton = {
                Button(
                    onClick = {

                        // Google event removal
                        if (isGoogleEvent) {
                            coroutineScope.launch {
                                try {
                                    calendarViewModel.deleteGoogleEvent(eventToDelete)

                                    // Display confirmation message
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
                            // Local event removal
                        } else {
                            // Remove from CalendarStorage
                            coroutineScope.launch(Dispatchers.IO) {
                                storage.removeEvent(eventToDelete)

                                // Update calendar screen
                                val updatedEvents = storage.loadEvents()
                                withContext(Dispatchers.Main) {
                                    savedEvents.clear()
                                    savedEvents.addAll(updatedEvents)

                                    // Display confirmation message
                                    Toast.makeText(
                                        context,
                                        "${eventToDelete.title} removed from Phin calendar",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // Close dialog box
                                    showRemoveDialog.value = false
                                }
                            }
                        }
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryRed,
                        contentColor = Color.White
                    )
                ) {
                    // Confirm button for removing event
                    Text(
                        text = "Remove from calendar",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            // Canceling the removal of event request
            dismissButton = {
                TextButton(
                    onClick = { showRemoveDialog.value = false }
                ) {
                    Text(
                        text = "Cancel",
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        )
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
