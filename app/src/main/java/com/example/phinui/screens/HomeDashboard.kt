package com.example.phinui.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.phinui.ui.components.HomeActionItem

@Composable
fun HomeDashboard(
    onOpenEvents: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(60.dp)
        ) {
            HomeActionItem(
                icon = Icons.Default.LocationOn,
                label = "Map",
                onClick = { }
            )

            HomeActionItem(
                icon = Icons.Default.DateRange,
                label = "Calendar",
                onClick = { }
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(60.dp)
        ) {
            HomeActionItem(
                icon = Icons.Default.Schedule,
                label = "Schedule",
                onClick = { }
            )

            HomeActionItem(
                icon = Icons.Default.Event,
                label = "Events",
                onClick = onOpenEvents
            )
        }
    }
}