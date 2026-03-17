package com.example.phinui.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.phinui.data.calendar.CalendarEvent
import com.example.phinui.data.events.EventData.formatEventDateTime
import com.example.phinui.ui.theme.Background
import com.example.phinui.ui.theme.HeaderRed
import com.example.phinui.ui.theme.HeaderText
import com.example.phinui.ui.theme.NavText
import com.example.phinui.ui.theme.SelectedPill

@Composable
fun EventsScreen(
    events: List<CalendarEvent>,
    onEventClick: (CalendarEvent) -> Unit,
    onAddEventClick: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedEvent by remember { mutableStateOf<CalendarEvent?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(top = 24.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = "Events",
                    fontSize = 28.sp,
                    color = NavText
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            items(events) { event ->
                EventCard(
                    title = event.title,
                    dateTime = formatEventDateTime(event),
                    location = event.location ?: "Location: TBD",
                    onClick = {
                        selectedEvent = event
                        showDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        FloatingActionButton(
            onClick = onAddEventClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 20.dp),
            containerColor = HeaderRed,
            contentColor = HeaderText
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Event"
            )
        }
    }

    if (showDialog && selectedEvent != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add to calendar?") },
            text = { Text("Do you want to add \"${selectedEvent!!.title}\" to your calendar?") },
            confirmButton = {
                Button(
                    onClick = {
                        onEventClick(selectedEvent!!)
                        showDialog = false
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showDialog = false
                    }
                ) {
                    Text("No")
                }
            }
        )
    }
}

@Composable
fun EventCard(
    title: String,
    dateTime: String,
    location: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SelectedPill)
            .padding(20.dp)
            .clickable { onClick() }
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = NavText
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = dateTime,
            fontSize = 16.sp,
            color = NavText
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = location,
            fontSize = 16.sp,
            color = NavText
        )
    }
}