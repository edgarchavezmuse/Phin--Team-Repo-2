package com.example.phinui.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.phinui.data.schedule.ScheduleClass
import com.example.phinui.ui.components.widgets.AddScheduleSheet
import com.example.phinui.ui.theme.Background
import com.example.phinui.ui.theme.NavText
import com.example.phinui.ui.viewmodel.ScheduleViewModel
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.material3.TextButton
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.IconButton
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlin.math.abs
import kotlin.math.roundToInt
import android.widget.Toast

@Composable
fun ScheduleScreen() {
    val scheduleViewModel: ScheduleViewModel = viewModel()
    val classes by scheduleViewModel.classes.collectAsState()
    val catalogCourses by scheduleViewModel.catalogCourses.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    var classToDelete by remember { mutableStateOf<ScheduleClass?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri")
    val accentRed = Color(0xFFFF1F1F)

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
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "Schedule",
                        fontSize = 28.sp,
                        color = NavText,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Your weekly class schedule",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showAddSheet = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, accentRed)
                    ) {
                        Text(
                            text = "+ Add Class",
                            color = accentRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (classes.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            text = "No classes added yet.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        Column(
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = day,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (dayClasses.isNotEmpty()) {
                                Text(
                                    text = if (dayClasses.size == 1) "1 class" else "${dayClasses.size} classes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (dayClasses.isEmpty()) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                color = Color(0xFFFFFAFA)
                            ) {
                                Text(
                                    text = "No classes",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                                )
                            }
                        }
                    } else {
                        items(dayClasses) { scheduleClass ->
                            SwipeToDeleteScheduleCard(
                                scheduleClass = scheduleClass,
                                accentRed = accentRed,
                                onRequestDelete = {
                                    classToDelete = scheduleClass
                                }
                            )
                        }
                    }
                }
            }
        }

        if (showAddSheet) {
            AddScheduleSheet(
                catalogCourses = catalogCourses,
                onDismiss = { showAddSheet = false },
                onSave = { scheduleClass ->
                    scheduleViewModel.addClass(scheduleClass) {
                        showAddSheet = false

                        Toast.makeText(
                            context,
                            "${scheduleClass.courseCode} added to schedule",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }

        if (classToDelete != null) {
            Dialog(onDismissRequest = { classToDelete = null }) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        Text(
                            text = "Remove class?",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = buildAnnotatedString {
                                append("Are you sure you want to remove ")

                                withStyle(
                                    SpanStyle(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    append("${classToDelete?.courseCode} ${classToDelete?.courseName}")
                                }

                                append(" from your schedule?")
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { classToDelete = null }) {
                                Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                            }

                            TextButton(
                                onClick = {
                                    classToDelete?.let {
                                        scheduleViewModel.deleteClass(it)

                                        Toast.makeText(
                                            context,
                                            "${it.courseCode} removed from schedule",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    classToDelete = null
                                }
                            ) {
                                Text("Remove", color = accentRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleClassCard(
    scheduleClass: ScheduleClass,
    accentRed: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .padding(start = 10.dp, top = 12.dp, bottom = 12.dp)
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(
                        color = accentRed,
                        shape = RoundedCornerShape(999.dp)
                    )
            )

            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "${scheduleClass.courseCode} • ${scheduleClass.courseName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
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
}

@Composable
private fun SwipeToDeleteScheduleCard(
    scheduleClass: ScheduleClass,
    accentRed: Color,
    onRequestDelete: () -> Unit
) {
    val actionWidth = 72.dp
    val density = LocalDensity.current
    val actionWidthPx = with(density) { actionWidth.toPx() }

    var offsetX by remember { mutableFloatStateOf(0f) }

    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        label = "scheduleCardOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Background action
        Box(
            modifier = Modifier
                .matchParentSize(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(actionWidth),
                shape = RoundedCornerShape(18.dp),
                color = accentRed.copy(alpha = 0.12f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onRequestDelete) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete class",
                            tint = accentRed
                        )
                    }
                }
            }
        }

        // Foreground card
        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()

                            offsetX = (offsetX + dragAmount)
                                .coerceIn(-actionWidthPx, 0f)
                        },
                        onDragEnd = {
                            offsetX = if (abs(offsetX) > actionWidthPx / 2f) {
                                -actionWidthPx
                            } else {
                                0f
                            }
                        }
                    )
                }
        ) {
            ScheduleClassCard(
                scheduleClass = scheduleClass,
                accentRed = accentRed
            )
        }
    }
}