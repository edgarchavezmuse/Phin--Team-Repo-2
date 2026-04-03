package com.example.phinui.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.phinui.data.calendar.CalendarEvent
import com.example.phinui.ui.components.calendar.EventCard
import com.example.phinui.ui.theme.Background
import com.example.phinui.ui.theme.NavText

@Composable
fun EventsScreen(
    events: List<CalendarEvent>,
    onEventClick: (CalendarEvent) -> Unit
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
                    event = event,
                    showDate = true,
                    onClick = {
                        selectedEvent = event
                        showDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
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