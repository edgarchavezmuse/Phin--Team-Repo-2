package com.example.phinui.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.phinui.data.calendar.CalendarEvent
import com.example.phinui.ui.theme.Background
import com.example.phinui.ui.theme.NavText
import com.example.phinui.data.calendar.CalendarSource

@Composable
fun AddEventScreen(
    onSaveEvent: (CalendarEvent) -> Unit,
    onBackClick: () -> Unit
) {

    var eventName by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf("") }
    var eventStartTime by remember { mutableStateOf("") }
    var eventEndTime by remember { mutableStateOf("") }
    var eventLocation by remember { mutableStateOf("") }
    var eventDescription by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {

        Text(
            text = "Add Event",
            fontSize = 28.sp,
            color = NavText
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = eventName,
            onValueChange = { eventName = it },
            label = { Text("Event Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = eventDate,
            onValueChange = { eventDate = it },
            label = { Text("Date (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = eventStartTime,
            onValueChange = { eventStartTime = it },
            label = { Text("Start Time (HH:MM)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = eventEndTime,
            onValueChange = { eventEndTime = it },
            label = { Text("End Time (HH:MM)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = eventLocation,
            onValueChange = { eventLocation = it },
            label = { Text("Location") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = eventDescription,
            onValueChange = { eventDescription = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val trimmedName = eventName.trim()
                val trimmedDate = eventDate.trim()
                val trimmedStartTime = eventStartTime.trim()
                val trimmedEndTime = eventEndTime.trim()
                val trimmedLocation = eventLocation.trim()
                val trimmedDescription = eventDescription.trim()

                if (
                    trimmedName.isBlank() ||
                    trimmedDate.isBlank() ||
                    trimmedStartTime.isBlank() ||
                    trimmedEndTime.isBlank()
                ) {
                    return@Button
                }

                if ("${trimmedDate}T${trimmedEndTime}" <= "${trimmedDate}T${trimmedStartTime}") {
                    return@Button
                }

                val newEvent = CalendarEvent(
                    id = System.currentTimeMillis().toString(),
                    title = trimmedName,
                    start = "${trimmedDate}T${trimmedStartTime}",
                    end = "${trimmedDate}T${trimmedEndTime}",
                    location = trimmedLocation.ifBlank { null },
                    reminderMinutes = emptyList(),
                    source = CalendarSource.LOCAL,
                    description = trimmedDescription.ifBlank { null }
                )

                onSaveEvent(newEvent)
            }
        ) {
            Text("Save Event")
        }
    }
}