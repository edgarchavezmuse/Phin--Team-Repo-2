package com.example.phinui.ui.components.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val formatter = DateTimeFormatter.ofPattern("MMM d")

// Handles Calendar header title + refresh button
@Composable
fun CalendarHeader(
    currentWeekStartDate: LocalDate,
    onRefreshClick: () -> Unit,
    isRefreshing: Boolean,
    isSignedIn: Boolean
) {
    val endDate = currentWeekStartDate.plusDays(6)

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${currentWeekStartDate.format(formatter)} - ${endDate.format(formatter)}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(12.dp))

            TextButton(
                onClick = onRefreshClick,
                enabled = !isRefreshing,
            ) {
                if (isSignedIn) {
                    TextButton(
                        onClick = onRefreshClick,
                        enabled = !isRefreshing,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(if (isRefreshing) "Refreshing..." else "Refresh")
                    }
                }
            }
        }
    }
}