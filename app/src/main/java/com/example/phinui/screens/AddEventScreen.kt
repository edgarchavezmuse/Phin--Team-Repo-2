package com.example.phinui.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.phinui.components.calendar.event_form.EventInputRow
import com.example.phinui.components.calendar.event_form.EventPickerRow
import com.example.phinui.components.calendar.event_form.EventTextArea
import com.example.phinui.data.calendar.CalendarEvent
import com.example.phinui.data.calendar.CalendarSource
import com.example.phinui.ui.theme.Background
import com.example.phinui.ui.theme.NavText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TimePickerDefaults

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreen(
    existingEvent: CalendarEvent? = null,
    onSaveEvent: (CalendarEvent) -> Unit,
    onBackClick: () -> Unit
) {
    var eventName by rememberSaveable(existingEvent?.id) {
        mutableStateOf(existingEvent?.title ?: "")
    }
    var eventDate by rememberSaveable(existingEvent?.id) {
        mutableStateOf(existingEvent?.let { extractEventDate(it) } ?: "")
    }
    var eventLocation by rememberSaveable(existingEvent?.id) {
        mutableStateOf(existingEvent?.location ?: "")
    }
    var eventDescription by rememberSaveable(existingEvent?.id) {
        mutableStateOf(existingEvent?.description ?: "")
    }
    var eventStartTime by rememberSaveable(existingEvent?.id) {
        mutableStateOf(existingEvent?.let { extractStartTime12Hour(it) } ?: "")
    }
    var eventEndTime by rememberSaveable(existingEvent?.id) {
        mutableStateOf(existingEvent?.let { extractEndTime12Hour(it) } ?: "")
    }
    var isAllDay by rememberSaveable(existingEvent?.id) {
        mutableStateOf(existingEvent?.isAllDay ?: false)
    }
    var selectedReminder by rememberSaveable(existingEvent?.id) {
        mutableStateOf(existingEvent?.reminderMinutes?.firstOrNull())
    }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showReminderMenu by remember { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val reminderOptions = listOf(
        "None" to null,
        "At time of event" to 0,
        "5 minutes before" to 5,
        "10 minutes before" to 10,
        "30 minutes before" to 30,
        "1 hour before" to 60,
        "1 day before" to 1440,
        "Custom..." to Int.MIN_VALUE
    )
    var showCustomReminderDialog by remember { mutableStateOf(false) }
    var customReminderAmount by rememberSaveable { mutableStateOf("") }
    var customReminderUnit by rememberSaveable { mutableStateOf("Minutes") }
    var showCustomReminderUnitMenu by remember { mutableStateOf(false) }
    var customReminderError by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedColorHex by rememberSaveable(existingEvent?.id) {
        mutableStateOf(existingEvent?.colorHex ?: "#FF1F1F")
    }
    var showColorPicker by rememberSaveable { mutableStateOf(false) }

    val presetColors = listOf(
        "#DC2127", // Red
        "#FBD75B", // Yellow
        "#51B749", // Green
        "#5484ED", // Blue
        "#DBADFF", // Lavender
        "#46D6DB"  // Teal
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = if (existingEvent == null) "Add Event" else "Edit Event",
            fontSize = 28.sp,
            color = MaterialTheme.colorScheme.onTertiary
        )

        Spacer(modifier = Modifier.height(24.dp))

        EventInputRow(
            label = "Event Name",
            value = eventName,
            placeholder = "Enter event name",
            onValueChange = {
                eventName = it
                errorMessage = null
            },
            isActive = false,
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

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable { isAllDay = !isAllDay }
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "All-day",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onTertiary
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "No start or end time",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF8A847D)
                )
            }

            Switch(
                checked = isAllDay,
                onCheckedChange = { isAllDay = it },
                colors = androidx.compose.material3.SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.surface,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.primary,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surface,
                    uncheckedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        if (!isAllDay) {
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

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showReminderMenu = true },
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Reminder",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF6F6A64)
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = selectedReminder?.let { formatSingleReminderLabel(it) } ?: "None",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onTertiary
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Reminder",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            DropdownMenu(
                expanded = showReminderMenu,
                onDismissRequest = { showReminderMenu = false },
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(18.dp)
                    ),
                containerColor = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                reminderOptions.forEach { (label, value) ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = label,
                                color = MaterialTheme.colorScheme.onTertiary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        onClick = {
                            showReminderMenu = false

                            if (value == Int.MIN_VALUE) {
                                customReminderAmount = ""
                                customReminderUnit = "Minutes"
                                customReminderError = null
                                showCustomReminderDialog = true
                            } else {
                                selectedReminder = value
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Accent Color",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onTertiary
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showColorPicker = !showColorPicker },
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .height(22.dp)
                                .background(
                                    color = Color(android.graphics.Color.parseColor(selectedColorHex)),
                                    shape = RoundedCornerShape(999.dp)
                                )
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Tap to choose a color",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF6F6A64)
                        )
                    }

                    Text(
                        text = if (showColorPicker) "Hide" else "Choose",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (showColorPicker) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        presetColors.forEach { colorHex ->
                            val swatchColor = Color(android.graphics.Color.parseColor(colorHex))
                            val isSelected = selectedColorHex == colorHex

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .clickable {
                                        selectedColorHex = colorHex
                                        showColorPicker = false
                                    },
                                shape = RoundedCornerShape(14.dp),
                                color = swatchColor,
                                tonalElevation = if (isSelected) 4.dp else 0.dp,
                                shadowElevation = if (isSelected) 4.dp else 0.dp,
                                border = if (isSelected) {
                                    BorderStroke(2.dp, Color.Black)
                                } else null
                            ) {}
                        }
                    }
                }
            }
        }

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
                    !isValidDate(trimmedDate) -> errorMessage = "Date must be in YYYY-MM-DD format."
                    !isAllDay && trimmedStartTime.isBlank() -> errorMessage =
                        "Please select a start time."

                    !isAllDay && trimmedEndTime.isBlank() -> errorMessage =
                        "Please select an end time."

                    else -> {
                        val eventId = existingEvent?.id ?: System.currentTimeMillis().toString()
                        val eventSource = existingEvent?.source ?: CalendarSource.LOCAL
                        val newEvent = if (isAllDay) {
                            CalendarEvent(
                                id = eventId,
                                title = trimmedName,
                                start = trimmedDate,
                                end = trimmedDate,
                                location = trimmedLocation.ifBlank { null },
                                reminderMinutes = selectedReminder?.let { listOf(it) }
                                    ?: emptyList(),
                                source = eventSource,
                                description = trimmedDescription.ifBlank { null },
                                isAllDay = true,
                                colorHex = selectedColorHex
                            )
                        } else {
                            val start24 = convertTo24Hour(trimmedStartTime)
                            val end24 = convertTo24Hour(trimmedEndTime)

                            if (end24 <= start24) {
                                errorMessage = "End time must be after start time."
                                return@Button
                            }

                            CalendarEvent(
                                id = eventId,
                                title = trimmedName,
                                start = "${trimmedDate}T$start24",
                                end = "${trimmedDate}T$end24",
                                location = trimmedLocation.ifBlank { null },
                                reminderMinutes = selectedReminder?.let { listOf(it) }
                                    ?: emptyList(),
                                source = eventSource,
                                description = trimmedDescription.ifBlank { null },
                                isAllDay = false,
                                colorHex = selectedColorHex
                            )
                        }

                        onSaveEvent(newEvent)
                    }
                }
            }
        ) {
            Text(if (existingEvent == null) "Save Event" else "Update Event")
        }

        Spacer(modifier = Modifier.height(12.dp))
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

    if (showCustomReminderDialog) {
        val accentRed = MaterialTheme.colorScheme.primary
        val cardColor = MaterialTheme.colorScheme.surface
        val titleColor = MaterialTheme.colorScheme.onTertiary
        val bodyColor = MaterialTheme.colorScheme.onSurfaceVariant
        val fieldColor = MaterialTheme.colorScheme.surface

        AlertDialog(
            onDismissRequest = {
                showCustomReminderDialog = false
                customReminderError = null
            },
            containerColor = cardColor,
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = "Custom Reminder",
                    color = titleColor,
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column {
                    Text(
                        text = "Choose when you want to be reminded.",
                        color = bodyColor,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = customReminderAmount,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) {
                                customReminderAmount = input
                                customReminderError = null
                            }
                        },
                        label = { Text("Amount") },
                        placeholder = { Text("Enter number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentRed,
                            unfocusedBorderColor = MaterialTheme.colorScheme.surface,
                            focusedLabelColor = accentRed,
                            unfocusedLabelColor = MaterialTheme.colorScheme.surface,
                            cursorColor = accentRed,
                            focusedContainerColor = fieldColor,
                            unfocusedContainerColor = fieldColor
                        )
                    )

                    customReminderError?.let {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Unit",
                        color = titleColor,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCustomReminderUnitMenu = true },
                            shape = RoundedCornerShape(14.dp),
                            color = fieldColor,
                            tonalElevation = 0.dp,
                            shadowElevation = 0.dp
                        ) {
                            Text(
                                text = customReminderUnit,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                color = titleColor
                            )
                        }

                        DropdownMenu(
                            expanded = showCustomReminderUnitMenu,
                            onDismissRequest = { showCustomReminderUnitMenu = false },
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            listOf("Minutes", "Hours", "Days").forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit) },
                                    onClick = {
                                        customReminderUnit = unit
                                        customReminderError = null
                                        showCustomReminderUnitMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = customReminderAmount.toIntOrNull()

                        val error = when (customReminderUnit) {
                            "Minutes" -> if (amount == null || amount !in 1..59) "Minutes must be between 1 and 59." else null
                            "Hours" -> if (amount == null || amount !in 1..23) "Hours must be between 1 and 23." else null
                            "Days" -> if (amount == null || amount !in 1..30) "Days must be between 1 and 30." else null
                            else -> "Invalid reminder."
                        }

                        if (error != null) {
                            customReminderError = error
                        } else {
                            val totalMinutes = when (customReminderUnit) {
                                "Minutes" -> amount!!
                                "Hours" -> amount!! * 60
                                "Days" -> amount!! * 1440
                                else -> amount!!
                            }

                            selectedReminder = totalMinutes
                            customReminderError = null
                            showCustomReminderDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentRed,
                        contentColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCustomReminderDialog = false
                        customReminderError = null
                    }
                ) {
                    Text("Cancel", color = accentRed)
                }
            }
        )
    }
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

    val accentRed = MaterialTheme.colorScheme.primary
    val cardColor = MaterialTheme.colorScheme.surface
    val titleColor = MaterialTheme.colorScheme.onTertiary
    val bodyColor = Color(0xFF444444)

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

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (showDial) {
                        TimePicker(
                            state = timePickerState,
                            colors = TimePickerDefaults.colors(
                                timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                timeSelectorSelectedContentColor = MaterialTheme.colorScheme.tertiary,

                                timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.onSecondary,
                                timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface,

                                periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                periodSelectorSelectedContentColor = MaterialTheme.colorScheme.tertiary,

                                periodSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surface,
                                periodSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface,

                                selectorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    } else {
                        TimeInput(
                            state = timePickerState,
                            colors = TimePickerDefaults.colors(
                                timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                timeSelectorSelectedContentColor = MaterialTheme.colorScheme.tertiary,

                                timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.onSecondary,
                                timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface,

                                periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                periodSelectorSelectedContentColor = MaterialTheme.colorScheme.tertiary,

                                periodSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surface,
                                periodSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
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
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentRed,
                            contentColor = MaterialTheme.colorScheme.surface
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

private fun extractEventDate(event: CalendarEvent): String {
    return try {
        if (event.start.contains("T")) {
            event.start.substring(0, 10)
        } else {
            event.start
        }
    } catch (_: Exception) {
        ""
    }
}

private fun extractStartTime12Hour(event: CalendarEvent): String {
    if (event.isAllDay) return ""

    return try {
        val time24 = if (event.start.contains("T")) {
            event.start.substring(11, 16)
        } else {
            ""
        }

        if (time24.isBlank()) "" else format24To12Hour(time24)
    } catch (_: Exception) {
        ""
    }
}

private fun extractEndTime12Hour(event: CalendarEvent): String {
    if (event.isAllDay) return ""

    return try {
        val time24 = if (event.end.contains("T")) {
            event.end.substring(11, 16)
        } else {
            ""
        }

        if (time24.isBlank()) "" else format24To12Hour(time24)
    } catch (_: Exception) {
        ""
    }
}

private fun format24To12Hour(time24: String): String {
    return try {
        val (hourStr, minuteStr) = time24.split(":")
        val hour24 = hourStr.toInt()
        val minute = minuteStr.toInt()
        formatTo12Hour(hour24, minute)
    } catch (_: Exception) {
        ""
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun EventDatePickerDialog(
    initialDate: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val accentRed = MaterialTheme.colorScheme.primary

    val initialMillis = remember(initialDate) {
        parseIsoDateToMillis(initialDate)
    }

    val datePickerState = rememberDatePickerState(
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
                    contentColor = MaterialTheme.colorScheme.surface
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
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        DatePicker(
            state = datePickerState,
            showModeToggle = true,
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onTertiary,
                headlineContentColor = MaterialTheme.colorScheme.onTertiary,
                weekdayContentColor = Color.DarkGray,
                subheadContentColor = Color.DarkGray,
                selectedDayContainerColor = accentRed,
                selectedDayContentColor = MaterialTheme.colorScheme.surface,
                todayDateBorderColor = accentRed,
                todayContentColor = accentRed,
                selectedYearContainerColor = accentRed,
                selectedYearContentColor = MaterialTheme.colorScheme.surface,
                dayContentColor = MaterialTheme.colorScheme.onTertiary,
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

private fun formatSingleReminderLabel(minutes: Int): String {
    return when (minutes) {
        0 -> "At time of event"
        5 -> "5 minutes before"
        10 -> "10 minutes before"
        30 -> "30 minutes before"
        60 -> "1 hour before"
        1440 -> "1 day before"
        else -> when {
            minutes % 1440 == 0 -> {
                val days = minutes / 1440
                if (days == 1) "1 day before" else "$days days before"
            }
            minutes % 60 == 0 -> {
                val hours = minutes / 60
                if (hours == 1) "1 hour before" else "$hours hours before"
            }
            else -> {
                if (minutes == 1) "1 minute before" else "$minutes minutes before"
            }
        }
    }
}