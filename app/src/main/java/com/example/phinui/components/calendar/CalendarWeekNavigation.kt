package com.example.phinui.components.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


// Handles calendar navigation bar (prev + today + next)
@Composable
fun CalendarWeekNavigation(
    onPreviousWeek: () -> Unit,
    onTodayClick: () -> Unit,
    onNextWeek: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(onClick = onPreviousWeek) {
                Text("Previous")
            }

            Button(
                onClick = onTodayClick,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                )
            ) {
                Text("Today")
            }

            TextButton(onClick = onNextWeek) {
                Text("Next")
            }
        }
    }
}