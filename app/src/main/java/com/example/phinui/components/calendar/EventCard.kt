package com.example.phinui.ui.components.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.phinui.data.calendar.CalendarEvent
import com.example.phinui.data.calendar.CalendarSource
import com.example.phinui.data.calendar.formatEventTimeLine
import com.example.phinui.data.calendar.formatReminderText
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.material.icons.filled.CalendarToday

private val eventDateFormatter = DateTimeFormatter.ofPattern("MMM d")


// Handles calendar event card
@Composable
fun EventCard(
    event: CalendarEvent,
    onClick: () -> Unit,
    showDate: Boolean = false
) {
    val isGoogleEvent = event.source == CalendarSource.GOOGLE

    val accentColor = MaterialTheme.colorScheme.primary


    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(100.dp))
                    .background(accentColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = event.title.ifBlank { "(No title)" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                /* To ensure that the date only displays on event cards on event
                *  screen and not also calendar event card
                */
                if (showDate) {
                    Spacer(modifier = Modifier.height(4.dp))

                    InfoRow(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Date",
                                tint = accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        text = formatEventDate(event)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                InfoRow(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Time",
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    text = formatEventTimeLine(event)
                )

                val reminderText = formatReminderText(event.reminderMinutes)
                if (reminderText != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Reminder",
                                tint = accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        text = "Reminder: $reminderText"
                    )
                }

                event.location?.takeIf { it.isNotBlank() }?.let { location ->
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        text = location
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: @Composable () -> Unit,
    text: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.padding(top = 2.dp)
        ) {
            icon()
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Extracts date and formats to readable date - used to display date for events screen
private fun formatEventDate(event: CalendarEvent): String {
    val start = event.start
    val end = event.end

    if (start.isBlank()) return "TBD"

    fun parseLocalDate(dateText: String): LocalDate? {
        return try {
            if (dateText.contains("T")) {
                when (event.source) {
                    CalendarSource.GOOGLE -> OffsetDateTime.parse(dateText).toLocalDate()
                    CalendarSource.LOCAL -> LocalDateTime.parse(dateText).toLocalDate()
                }
            } else {
                LocalDate.parse(dateText)
            }
        } catch (_: Exception) {
            null
        }
    }

    val startDate = parseLocalDate(start) ?: return "TBD"
    val endDate = parseLocalDate(end)

    return if (endDate != null && endDate != startDate) {
        "${startDate.format(eventDateFormatter)} - ${endDate.format(eventDateFormatter)}"
    } else {
        startDate.format(eventDateFormatter)
    }
}