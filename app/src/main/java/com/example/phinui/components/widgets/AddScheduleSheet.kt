package com.example.phinui.ui.components.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.phinui.components.calendar.event_form.EventInputRow
import com.example.phinui.components.calendar.event_form.EventPickerRow
import com.example.phinui.data.schedule.ScheduleClass
import com.example.phinui.ui.components.time_pickers.EventTimePickerDialog
import com.example.phinui.ui.components.time_pickers.convertTo24Hour

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScheduleSheet(
    onDismiss: () -> Unit,
    onSave: (ScheduleClass) -> Unit
) {
    var courseCode by rememberSaveable { mutableStateOf("") }
    var courseName by rememberSaveable { mutableStateOf("") }
    var startTime by rememberSaveable { mutableStateOf("") }
    var endTime by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    val selectedDays = remember { mutableStateListOf<String>() }
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri")

    val accentRed = Color(0xFFFF1F1F)
    val chipBarColor = Color(0xFFFFEAEA)
    val unselectedChipColor = Color.White
    val selectedChipTextColor = Color.White
    val unselectedChipTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Color(0xFFFFFAFA)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Add Class",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Quickly add a course to your schedule",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            EventInputRow(
                label = "Course Code",
                value = courseCode,
                placeholder = "Enter course code",
                onValueChange = {
                    courseCode = it
                    errorMessage = null
                }
            )

            EventInputRow(
                label = "Course Name",
                value = courseName,
                placeholder = "Enter course name",
                onValueChange = {
                    courseName = it
                    errorMessage = null
                }
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Days",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = chipBarColor,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        days.forEach { day ->
                            val isSelected = selectedDays.contains(day)

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) accentRed else unselectedChipColor,
                                shadowElevation = if (isSelected) 0.dp else 1.dp,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        errorMessage = null
                                        if (isSelected) {
                                            selectedDays.remove(day)
                                        } else {
                                            selectedDays.add(day)
                                        }
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) {
                                            selectedChipTextColor
                                        } else {
                                            unselectedChipTextColor
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EventPickerRow(
                    label = "Start Time",
                    value = startTime,
                    placeholder = "Start",
                    icon = Icons.Default.Schedule,
                    isActive = showStartPicker,
                    onClick = {
                        errorMessage = null
                        showStartPicker = true
                    },
                    modifier = Modifier.weight(1f)
                )

                EventPickerRow(
                    label = "End Time",
                    value = endTime,
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

            EventInputRow(
                label = "Location",
                value = location,
                placeholder = "Add location",
                onValueChange = {
                    location = it
                    errorMessage = null
                },
                trailingIcon = Icons.Default.LocationOn
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        val trimmedCourseCode = courseCode.trim()
                        val trimmedCourseName = courseName.trim()
                        val trimmedStartTime = startTime.trim()
                        val trimmedEndTime = endTime.trim()
                        val trimmedLocation = location.trim()

                        when {
                            trimmedCourseCode.isBlank() -> {
                                errorMessage = "Please enter a course code."
                            }

                            trimmedCourseName.isBlank() -> {
                                errorMessage = "Please enter a course name."
                            }

                            selectedDays.isEmpty() -> {
                                errorMessage = "Please select at least one day."
                            }

                            trimmedStartTime.isBlank() -> {
                                errorMessage = "Please select a start time."
                            }

                            trimmedEndTime.isBlank() -> {
                                errorMessage = "Please select an end time."
                            }

                            else -> {
                                val start24 = convertTo24Hour(trimmedStartTime)
                                val end24 = convertTo24Hour(trimmedEndTime)

                                if (end24 <= start24) {
                                    errorMessage = "End time must be after start time."
                                } else {
                                    onSave(
                                        ScheduleClass(
                                            courseCode = trimmedCourseCode,
                                            courseName = trimmedCourseName,
                                            days = selectedDays.toList(),
                                            startTime = trimmedStartTime,
                                            endTime = trimmedEndTime,
                                            location = trimmedLocation
                                        )
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentRed,
                        contentColor = Color.White
                    )
                ) {
                    Text("Save Class")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (showStartPicker) {
        EventTimePickerDialog(
            title = "Start Time",
            initialTime = startTime,
            onDismiss = { showStartPicker = false },
            onConfirm = { selectedTime ->
                startTime = selectedTime
                showStartPicker = false
            }
        )
    }

    if (showEndPicker) {
        EventTimePickerDialog(
            title = "End Time",
            initialTime = endTime,
            onDismiss = { showEndPicker = false },
            onConfirm = { selectedTime ->
                endTime = selectedTime
                showEndPicker = false
            }
        )
    }
}