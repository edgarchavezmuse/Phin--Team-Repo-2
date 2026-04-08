package com.example.phinui.data.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.example.phinui.data.calendar.CalendarEvent
import com.example.phinui.data.calendar.formatEventTimeLine
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import java.time.format.DateTimeFormatter
import java.time.OffsetDateTime
import androidx.compose.material.icons.filled.CalendarToday
import com.example.phinui.data.calendar.eventDate


private val PrimaryRed = Color(0xFFE53935)
private val TextDark = Color(0xFF1F1F1F)
private val TextMuted = Color(0xFF000000)

private val eventDateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")


@Composable
fun EventDetails(event: CalendarEvent) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        eventDate(event)?.let { date ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Date",
                    tint = PrimaryRed
                )
                Text(
                    text = date.format(eventDateFormatter),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextDark,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = "Time",
                tint = PrimaryRed
            )
            Text(
                text = formatEventTimeLine(event),
                style = MaterialTheme.typography.bodyLarge,
                color = TextDark
                //fontWeight = FontWeight.SemiBold
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Location",
                tint = PrimaryRed
            )
            Text(
                text = event.location ?: "Location: TBD",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
        }

        Text(
            text = "Description",
            style = MaterialTheme.typography.labelLarge,
            color = TextMuted,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = event.description ?: "No description available.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )
    }
}