package com.example.phinui.ui.components.widgets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.phinui.data.schedule.CourseCatalogItem
import com.example.phinui.data.schedule.ScheduleClass
import com.example.phinui.ui.components.time_pickers.EventTimePickerDialog
import com.example.phinui.ui.components.time_pickers.convertTo24Hour

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScheduleSheet(
    catalogCourses: List<CourseCatalogItem>,
    editingClass: ScheduleClass? = null,
    onDismiss: () -> Unit,
    onSave: (ScheduleClass) -> Unit
) {
    val isEditMode = editingClass != null

    var courseSearch by rememberSaveable(editingClass?.id) { mutableStateOf("") }
    var courseCode by rememberSaveable(editingClass?.id) { mutableStateOf(editingClass?.courseCode ?: "") }
    var courseName by rememberSaveable(editingClass?.id) { mutableStateOf(editingClass?.courseName ?: "") }
    var startTime by rememberSaveable(editingClass?.id) { mutableStateOf(editingClass?.startTime ?: "") }
    var endTime by rememberSaveable(editingClass?.id) { mutableStateOf(editingClass?.endTime ?: "") }
    var location by rememberSaveable(editingClass?.id) { mutableStateOf(editingClass?.location ?: "") }
    var errorMessage by rememberSaveable(editingClass?.id) { mutableStateOf<String?>(null) }
    var selectedColorHex by rememberSaveable(editingClass?.id) {
        mutableStateOf(editingClass?.colorHex ?: "#FF1F1F")
    }
    var showColorPicker by rememberSaveable(editingClass?.id) { mutableStateOf(false) }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    val selectedDays = remember(editingClass?.id) { mutableStateListOf<String>() }

    LaunchedEffect(editingClass?.id) {
        selectedDays.clear()
        selectedDays.addAll(editingClass?.days ?: emptyList())
        courseSearch = if (editingClass != null) {
            "${editingClass.courseCode} - ${editingClass.courseName}"
        } else {
            ""
        }
    }

    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri")
    val presetColors = listOf(
        "#FF1F1F",
        "#FF9800",
        "#4CAF50",
        "#2196F3",
        "#9C27B0",
        "#FF69B4"
    )

    val accentRed = Color(0xFFFF1F1F)
    val chipBarColor = Color(0xFFFFEAEA)
    val unselectedChipColor = Color.White
    val selectedChipTextColor = Color.White
    val unselectedChipTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    val filteredCourses = remember(courseSearch, catalogCourses) {
        val query = courseSearch.trim().lowercase()

        if (query.isBlank()) {
            emptyList()
        } else {
            catalogCourses.filter { course ->
                course.code.lowercase().contains(query) ||
                        course.name.lowercase().contains(query)
            }.take(8)
        }
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        sheetState = sheetState,
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
                text = if (isEditMode) "Edit Class" else "Add Class",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = if (isEditMode) {
                    "Update the details for this class"
                } else {
                    "Quickly add a course to your schedule"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            EventInputRow(
                label = "Search Course",
                value = courseSearch,
                placeholder = "Search by code or name",
                onValueChange = {
                    courseSearch = it
                    errorMessage = null
                }
            )

            if (courseSearch.isNotBlank() && filteredCourses.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 220.dp)
                    ) {
                        items(filteredCourses) { course ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        courseSearch = "${course.code} - ${course.name}"
                                        courseCode = course.code
                                        courseName = course.name
                                        errorMessage = null
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text = course.code,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = course.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

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

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Accent Color",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showColorPicker = !showColorPicker
                        },
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    tonalElevation = 1.dp,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier
                                    .width(22.dp)
                                    .height(22.dp),
                                shape = RoundedCornerShape(999.dp),
                                color = Color(android.graphics.Color.parseColor(selectedColorHex))
                            ) {}

                            Text(
                                text = "Tap to choose a color",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = if (showColorPicker) "Hide" else "Choose",
                            style = MaterialTheme.typography.labelLarge,
                            color = accentRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (showColorPicker) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFDF4F4),
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
                                            id = editingClass?.id ?: "",
                                            courseCode = trimmedCourseCode,
                                            courseName = trimmedCourseName,
                                            days = selectedDays.toList(),
                                            startTime = trimmedStartTime,
                                            endTime = trimmedEndTime,
                                            location = trimmedLocation,
                                            colorHex = selectedColorHex
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
                    Text(if (isEditMode) "Update Class" else "Save Class")
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