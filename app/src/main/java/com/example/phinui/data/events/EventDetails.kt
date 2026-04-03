package com.example.phinui.data.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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

private val PrimaryRed = Color(0xFFE53935)
private val TextDark = Color(0xFF1F1F1F)
private val TextMuted = Color(0xFF666666)

@Composable
fun EventDetails(event: CalendarEvent) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = formatEventTimeLine(event),
            style = MaterialTheme.typography.bodyLarge,
            color = TextDark,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = event.location ?: "Location: TBD",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )

        Text(
            text = "About this event",
            style = MaterialTheme.typography.titleSmall,
            color = PrimaryRed,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = event.description ?: "No description available.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )
    }
}