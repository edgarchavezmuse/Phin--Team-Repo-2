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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.phinui.data.calendar.CalendarEvent
import com.example.phinui.ui.theme.Background
import com.example.phinui.ui.theme.NavText
import com.example.phinui.data.calendar.CalendarSource
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.platform.LocalContext
import android.app.TimePickerDialog

@Composable
fun AddEventScreen(
    onSaveEvent: (CalendarEvent) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var eventName by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf("") }
    var eventLocation by remember { mutableStateOf("") }
    var eventDescription by remember { mutableStateOf("") }
    var eventStartTime by remember { mutableStateOf("") }
    var eventEndTime by remember { mutableStateOf("") }

    val startTimePicker = {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                eventStartTime = formatTo12Hour(hourOfDay, minute)
            },
            9,
            0,
            false
        ).show()
    }

    val endTimePicker = {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                eventEndTime = formatTo12Hour(hourOfDay, minute)
            },
            10,
            0,
            false
        ).show()
    }

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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { startTimePicker() }
        ) {
            OutlinedTextField(
                value = eventStartTime,
                onValueChange = {},
                readOnly = true,
                enabled = false,
                label = { Text("Start Time") },
                placeholder = { Text("Select time") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { endTimePicker() }
        ) {
            OutlinedTextField(
                value = eventEndTime,
                onValueChange = {},
                readOnly = true,
                enabled = false,
                label = { Text("End Time") },
                placeholder = { Text("Select time") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

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
                    trimmedName.isNotBlank() &&
                    trimmedDate.isNotBlank() &&
                    trimmedStartTime.isNotBlank() &&
                    trimmedEndTime.isNotBlank()
                ) {
                    val start24 = convertTo24Hour(trimmedStartTime)
                    val end24 = convertTo24Hour(trimmedEndTime)

                    if ("${trimmedDate}T$end24" > "${trimmedDate}T$start24") {
                        val newEvent = CalendarEvent(
                            id = System.currentTimeMillis().toString(),
                            title = trimmedName,
                            start = "${trimmedDate}T$start24",
                            end = "${trimmedDate}T$end24",
                            location = trimmedLocation.ifBlank { null },
                            reminderMinutes = emptyList(),
                            source = CalendarSource.LOCAL,
                            description = trimmedDescription.ifBlank { null }
                        )

                        onSaveEvent(newEvent)
                    }
                }
            }
        ) {
            Text("Save Event")
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel")
        }
    }
}

private fun convertTo24Hour(time12: String): String {
    val parts = time12.split(" ")
    val time = parts[0]
    val period = parts[1]

    val (hourStr, minute) = time.split(":")
    var hour = hourStr.toInt()

    if (period == "PM" && hour != 12) {
        hour += 12
    } else if (period == "AM" && hour == 12) {
        hour = 0
    }

    return String.format("%02d:%s", hour, minute)
}

private fun formatTo12Hour(hour24: Int, minute: Int): String {
    val period = if (hour24 >= 12) "PM" else "AM"
    val hour12 = when {
        hour24 == 0 -> 12
        hour24 > 12 -> hour24 - 12
        else -> hour24
    }
    return String.format("%d:%02d %s", hour12, minute, period)
}