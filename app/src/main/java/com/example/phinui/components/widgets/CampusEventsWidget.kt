package com.example.phinui.ui.components.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.phinui.data.calendar.CalendarEvent
import com.example.phinui.data.calendar.CalendarSource
import com.example.phinui.data.calendar.formatEventTimeLine
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

private val dayFormatter = DateTimeFormatter.ofPattern("d")
private val monthFormatter = DateTimeFormatter.ofPattern("MMM")
private val fullDateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")

@Composable
fun CampusEventsWidget(
    events: List<CalendarEvent>,
    onViewAllClick: () -> Unit
) {
    val today = LocalDate.now()

    val todayEvents = events
        .filter { isEventOnDate(it, today) }
        .sortedBy { parseEventDateTime(it) }

    val upcomingEvents = events
        .filter { isUpcomingEvent(it) && !isEventOnDate(it, today) }
        .sortedBy { parseEventDateTime(it) }

    val showingToday = todayEvents.isNotEmpty()
    val visibleEvents = if (showingToday) todayEvents.take(3) else upcomingEvents.take(3)
    val extraTodayCount = (todayEvents.size - 3).coerceAtLeast(0)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (showingToday) "Today's Events" else "Upcoming Campus Events",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (showingToday) {
                            "What’s happening on campus today"
                        } else {
                            "What’s happening on campus next"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "See all",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(onClick = onViewAllClick)
                )
            }

            if (visibleEvents.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "No campus events right now",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Check the Events page for new campus activities.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                visibleEvents.forEachIndexed { index, event ->
                    ModernEventRow(event = event)

                    if (index != visibleEvents.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(top = 2.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }

                if (showingToday && extraTodayCount > 0) {
                    Text(
                        text = "+$extraTodayCount more today",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernEventRow(
    event: CalendarEvent
) {
    val dateTime = parseEventDateTime(event)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        DateBadge(dateTime = dateTime)

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = event.title.ifBlank { "(No title)" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = formatEventTimeLine(event),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val location = event.location?.takeIf { it.isNotBlank() }
            if (location != null) {
                Text(
                    text = location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = formatFullDate(event),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DateBadge(
    dateTime: LocalDateTime?
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
    ) {
        Column(
            modifier = Modifier
                .width(58.dp)
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = dateTime?.format(monthFormatter)?.uppercase() ?: "--",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = dateTime?.format(dayFormatter) ?: "--",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun isUpcomingEvent(event: CalendarEvent): Boolean {
    val eventDateTime = parseEventDateTime(event) ?: return false
    return !eventDateTime.isBefore(LocalDateTime.now())
}

private fun isEventOnDate(event: CalendarEvent, date: LocalDate): Boolean {
    val eventDateTime = parseEventDateTime(event) ?: return false
    return eventDateTime.toLocalDate() == date
}

private fun parseEventDateTime(event: CalendarEvent): LocalDateTime? {
    val start = event.start
    if (start.isBlank()) return null

    return try {
        when (event.source) {
            CalendarSource.GOOGLE -> {
                if (start.contains("T")) {
                    OffsetDateTime.parse(start).toLocalDateTime()
                } else {
                    null
                }
            }
            CalendarSource.LOCAL -> {
                if (start.contains("T")) {
                    LocalDateTime.parse(start)
                } else {
                    null
                }
            }
        }
    } catch (_: Exception) {
        null
    }
}

private fun formatFullDate(event: CalendarEvent): String {
    val date = parseEventDateTime(event) ?: return "TBD"
    return date.format(fullDateFormatter)
}