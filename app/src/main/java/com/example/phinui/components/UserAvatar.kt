package com.example.phinui.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun UserAvatar(
    name: String,
    size: Int = 44
) {
    val initial = name.firstOrNull()?.uppercase() ?: "?"

    Surface(
        modifier = Modifier.size(size.dp),
        shape = CircleShape,
        color = Color(0xFFFFEFEF)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = initial,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFD32F2F)
            )
        }
    }
}