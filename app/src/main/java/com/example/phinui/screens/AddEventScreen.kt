package com.example.phinui.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.phinui.data.calendar.CalendarEvent
import com.example.phinui.data.calendar.CalendarSource
import com.example.phinui.ui.theme.Background
import com.example.phinui.ui.theme.NavText
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import com.example.phinui.components.calendar.event_form.EventInputRow
import com.example.phinui.components.calendar.event_form.EventPickerRow
import com.example.phinui.components.calendar.event_form.EventTextArea
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreen(
    onSaveEvent: (CalendarEvent) -> Unit,
    onBackClick: () -> Unit
) {
    var eventName by rememberSaveable { mutableStateOf("") }
    var eventDate by rememberSaveable { mutableStateOf("") }
    var eventLocation by rememberSaveable { mutableStateOf("") }
    var eventDescription by rememberSaveable { mutableStateOf("") }
    var eventStartTime by rememberSaveable { mutableStateOf("") }
    var eventEndTime by rememberSaveable { mutableStateOf("") }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Add Event",
            fontSize = 28.sp,
            color = NavText
        )

        Spacer(modifier = Modifier.height(24.dp))

        var eventNameFocused by remember { mutableStateOf(false) }

        EventInputRow(
            label = "Event Name",
            value = eventName,
            placeholder = "Enter event name",
            onValueChange = {
                eventName = it
                errorMessage = null
            },
            isActive = eventNameFocused,
            modifier = Modifier
        )

        Spacer(modifier = Modifier.height(16.dp))

        EventPickerRow(
            label = "Date",
            value = if (eventDate.isBlank()) "" else formatDisplayDate(eventDate),
            placeholder = "Select date",
            icon = Icons.Default.DateRange,
            isActive = showDatePicker,
            onClick = {
                errorMessage = null
                showDatePicker = true
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EventPickerRow(
                label = "Start",
                value = eventStartTime,
                placeholder = "Start",
                icon = Icons.Default.Schedule,
                isActive = showStartPicker,
                onClick = {
                    errorMessage = null
                    showStartPicker = true
                },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "to",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF7A766F)
            )

            Spacer(modifier = Modifier.width(8.dp))

            EventPickerRow(
                label = "End",
                value = eventEndTime,
                placeholder = "End",
                icon = Icons.Default.Schedule,
                isActive = showEndPicker,
                onClick = {
                    errorMessage = null
                    showEndPicker = true
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        EventInputRow(
            label = "Location",
            value = eventLocation,
            placeholder = "Add location",
            onValueChange = { eventLocation = it },
            trailingIcon = Icons.Default.LocationOn
        )

        Spacer(modifier = Modifier.height(16.dp))

        EventTextArea(
            label = "Description",
            value = eventDescription,
            placeholder = "Add notes about the event",
            onValueChange = { eventDescription = it }
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val trimmedName = eventName.trim()
                val trimmedDate = eventDate.trim()
                val trimmedStartTime = eventStartTime.trim()
                val trimmedEndTime = eventEndTime.trim()
                val trimmedLocation = eventLocation.trim()
                val trimmedDescription = eventDescription.trim()

                when {
                    trimmedName.isBlank() -> errorMessage = "Please enter an event name."
                    trimmedDate.isBlank() -> errorMessage = "Please enter a date."
                    trimmedStartTime.isBlank() -> errorMessage = "Please select a start time."
                    trimmedEndTime.isBlank() -> errorMessage = "Please select an end time."
                    !isValidDate(trimmedDate) -> errorMessage = "Date must be in YYYY-MM-DD format."
                    else -> {
                        val start24 = convertTo24Hour(trimmedStartTime)
                        val end24 = convertTo24Hour(trimmedEndTime)

                        if (end24 <= start24) {
                            errorMessage = "End time must be after start time."
                        } else {
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

    if (showStartPicker) {
        EventTimePickerDialog(
            title = "Start Time",
            initialTime = eventStartTime,
            onDismiss = { showStartPicker = false },
            onConfirm = { selectedTime ->
                eventStartTime = selectedTime
                showStartPicker = false
            }
        )
    }

    if (showEndPicker) {
        EventTimePickerDialog(
            title = "End Time",
            initialTime = eventEndTime,
            onDismiss = { showEndPicker = false },
            onConfirm = { selectedTime ->
                eventEndTime = selectedTime
                showEndPicker = false
            }
        )
    }

    if (showDatePicker) {
        EventDatePickerDialog(
            initialDate = eventDate,
            onDismiss = { showDatePicker = false },
            onConfirm = { selectedDate ->
                eventDate = selectedDate
                showDatePicker = false
            }
        )
    }
}

@Composable
private fun TimeField(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    val accentRed = androidx.compose.ui.graphics.Color(0xFFFF1F1F)
    val textDark = androidx.compose.ui.graphics.Color(0xFF111111)
    val hintGray = androidx.compose.ui.graphics.Color(0xFF777777)

    OutlinedTextField(
        value = formatDisplayDate(value),
        onValueChange = {},
        readOnly = true,
        enabled = false,
        label = { Text(label) },
        placeholder = { Text("Select time") },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = accentRed
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            disabledBorderColor = hintGray,
            disabledLabelColor = hintGray,
            disabledTextColor = textDark,
            disabledTrailingIconColor = accentRed
        )
    )
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun EventTimePickerDialog(
    title: String,
    initialTime: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val initialHour24 = remember(initialTime) {
        if (initialTime.isBlank()) 9 else convertTo24Hour(initialTime).substringBefore(":").toInt()
    }
    val initialMinute = remember(initialTime) {
        if (initialTime.isBlank()) 0 else convertTo24Hour(initialTime).substringAfter(":").toInt()
    }

    val timePickerState = rememberTimePickerState(
        initialHour = initialHour24,
        initialMinute = initialMinute,
        is24Hour = false
    )

    var showDial by rememberSaveable { mutableStateOf(false) }

    val accentRed = androidx.compose.ui.graphics.Color(0xFFFF1F1F)
    val cardColor = androidx.compose.ui.graphics.Color(0xFFF7F5F2)
    val titleColor = androidx.compose.ui.graphics.Color(0xFF111111)
    val bodyColor = androidx.compose.ui.graphics.Color(0xFF444444)

    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = cardColor,
            shadowElevation = 10.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = titleColor,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(onClick = { showDial = !showDial }) {
                        Icon(
                            imageVector = if (showDial) Icons.Outlined.Edit else Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = accentRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (showDial) "Dial picker" else "Enter time",
                    style = MaterialTheme.typography.bodyMedium,
                    color = bodyColor
                )

                Spacer(modifier = Modifier.height(18.dp))

                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (showDial) {
                        TimePicker(state = timePickerState)
                    } else {
                        TimeInput(state = timePickerState)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = accentRed)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            onConfirm(
                                formatTo12Hour(
                                    hour24 = timePickerState.hour,
                                    minute = timePickerState.minute
                                )
                            )
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = accentRed,
                            contentColor = androidx.compose.ui.graphics.Color.White
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

private fun convertTo24Hour(time12: String): String {
    val parts = time12.trim().split(" ")
    val time = parts[0]
    val period = parts[1]

    val (hourStr, minuteStr) = time.split(":")
    var hour = hourStr.toInt()

    if (period == "PM" && hour != 12) {
        hour += 12
    } else if (period == "AM" && hour == 12) {
        hour = 0
    }

    return String.format("%02d:%02d", hour, minuteStr.toInt())
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

private fun isValidDate(date: String): Boolean {
    val regex = Regex("""\d{4}-\d{2}-\d{2}""")
    return regex.matches(date)
}

@Composable
private fun DateField(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    val accentRed = androidx.compose.ui.graphics.Color(0xFFFF1F1F)
    val textDark = androidx.compose.ui.graphics.Color(0xFF111111)
    val hintGray = androidx.compose.ui.graphics.Color(0xFF777777)

    val displayValue = if (value.isBlank()) "" else formatDisplayDate(value)

    OutlinedTextField(
        value = displayValue,
        onValueChange = {},
        readOnly = true,
        enabled = false,
        label = { Text(label) },
        placeholder = { Text("Select date") },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = accentRed
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            disabledBorderColor = hintGray,
            disabledLabelColor = hintGray,
            disabledTextColor = textDark,
            disabledTrailingIconColor = accentRed
        )
    )
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun EventDatePickerDialog(
    initialDate: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val accentRed = Color(0xFFFF1F1F)
    val cardColor = Color(0xFFF7F5F2)

    val initialMillis = remember(initialDate) {
        parseIsoDateToMillis(initialDate)
    }

    val datePickerState = androidx.compose.material3.rememberDatePickerState(
        initialSelectedDateMillis = initialMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        onConfirm(formatMillisToIsoDate(millis))
                    }
                },
                enabled = datePickerState.selectedDateMillis != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentRed,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = accentRed)
            }
        },
        colors = DatePickerDefaults.colors(
            containerColor = Color.White, // 👈 THIS fixes purple background
        )
    ) {
        DatePicker(
            state = datePickerState,
            showModeToggle = true,
            colors = DatePickerDefaults.colors(
                containerColor = Color.White, // 👈 important
                titleContentColor = Color.Black,
                headlineContentColor = Color.Black,
                weekdayContentColor = Color.DarkGray,
                subheadContentColor = Color.DarkGray,

                selectedDayContainerColor = accentRed,
                selectedDayContentColor = Color.White,

                todayDateBorderColor = accentRed,
                todayContentColor = accentRed,

                selectedYearContainerColor = accentRed,
                selectedYearContentColor = Color.White,

                dayContentColor = Color.Black,
                disabledDayContentColor = Color.LightGray
            )
        )
    }
}

private fun formatMillisToIsoDate(millis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date(millis))
}

private fun parseIsoDateToMillis(date: String): Long? {
    return try {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        formatter.parse(date)?.time
    } catch (_: Exception) {
        null
    }
}

private fun formatDisplayDate(date: String): String {
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val output = SimpleDateFormat("MMM d, yyyy", Locale.US)
        val parsed = input.parse(date)
        if (parsed != null) output.format(parsed) else date
    } catch (e: Exception) {
        date
    }
}