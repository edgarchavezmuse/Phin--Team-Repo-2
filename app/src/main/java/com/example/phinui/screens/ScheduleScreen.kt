package com.example.phinui.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.phinui.data.schedule.ScheduleClass
import com.example.phinui.ui.theme.Background
import com.example.phinui.ui.theme.NavText
import com.example.phinui.ui.viewmodel.ScheduleViewModel

@Composable
fun ScheduleScreen() {
    val scheduleViewModel: ScheduleViewModel = viewModel()
    val classes by scheduleViewModel.classes.collectAsState()

    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri")

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Schedule",
                    fontSize = 28.sp,
                    color = NavText,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (classes.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        tonalElevation = 2.dp
                    ) {
                        Text(
                            text = "No classes added yet.",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }
            } else {
                days.forEach { day ->
                    val dayClasses = classes
                        .filter { it.days.contains(day) }
                        .sortedBy { it.startTime }

                    item {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.titleMedium,
                            color = NavText,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }

                    if (dayClasses.isEmpty()) {
                        item {
                            Text(
                                text = "No classes",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    } else {
                        items(dayClasses) { scheduleClass ->
                            ScheduleClassCard(scheduleClass = scheduleClass)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleClassCard(scheduleClass: ScheduleClass) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "${scheduleClass.courseCode} • ${scheduleClass.courseName}",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "${scheduleClass.startTime} - ${scheduleClass.endTime}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (scheduleClass.location.isNotBlank()) {
                Text(
                    text = scheduleClass.location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}