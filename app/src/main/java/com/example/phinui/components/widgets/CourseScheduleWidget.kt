package com.example.phinui.ui.components.widgets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.phinui.data.schedule.ScheduleClass
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

private val TextDark = Color(0xFF1E1E1E)
private val TextMuted = Color(0xFF6F6F6F)
private val BrandRed = Color(0xFFFF1F1F)

@Composable
fun CourseScheduleWidget(
    classes: List<ScheduleClass>,
    onAddClass: () -> Unit,
    onViewSchedule: () -> Unit,
    onDeleteClass: (ScheduleClass) -> Unit // ✅ NEW
) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri")
    var selectedDay by remember { mutableStateOf("Mon") }

    val filteredClasses = classes
        .filter { it.days.contains(selectedDay) }
        .sortedBy { it.startTime }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Header
            Column {
                Text(
                    text = "Course Schedule Builder",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Build and manage your class schedule",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            }

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onAddClass,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, BrandRed)
                ) {
                    Text("+ Add Class", color = BrandRed)
                }

                Button(
                    onClick = onViewSchedule,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                ) {
                    Text("View Schedule")
                }
            }

            // Day selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFEAEA), RoundedCornerShape(16.dp))
                    .padding(6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                days.forEach { day ->
                    val isSelected = day == selectedDay

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        modifier = Modifier.clickable { selectedDay = day }
                    ) {
                        Text(
                            text = day,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Content
            if (filteredClasses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No classes added yet", color = TextMuted)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                    filteredClasses.take(3).forEach { scheduleClass ->

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 4.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.fillMaxWidth()) {

                                IconButton(
                                    onClick = { onDeleteClass(scheduleClass) },
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = "Delete class",
                                        tint = BrandRed
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    // Left accent bar
                                    Box(
                                        modifier = Modifier
                                            .padding(start = 10.dp)
                                            .width(5.dp)
                                            .height(72.dp)
                                            .background(BrandRed, RoundedCornerShape(12.dp))
                                    )

                                    // Class info
                                    Column(
                                        modifier = Modifier
                                            .padding(14.dp)
                                            .weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "${scheduleClass.courseCode} • ${scheduleClass.courseName}",
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextDark
                                        )

                                        Text(
                                            text = "${scheduleClass.startTime} - ${scheduleClass.endTime}",
                                            color = TextMuted
                                        )

                                        if (scheduleClass.location.isNotBlank()) {
                                            Text(scheduleClass.location, color = TextMuted)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}