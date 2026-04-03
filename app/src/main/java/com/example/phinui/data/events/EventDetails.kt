package com.example.phinui.data.events

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import com.example.phinui.data.calendar.CalendarEvent
import androidx.compose.material3.Text
import com.example.phinui.data.calendar.formatEventTimeLine
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp


// Function for displaying the event information in the description box
@Composable
fun EventDetails(event: CalendarEvent){
    Column {
        Text(
            text = formatEventTimeLine(event)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = event.location ?: "Location: TBD"
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = event.description ?: "No description available."
        )
    }
}