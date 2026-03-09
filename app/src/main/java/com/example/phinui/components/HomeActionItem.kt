package com.example.phinui.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.phinui.ui.theme.NavText

@Composable
fun HomeActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = NavText,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            fontSize = 20.sp,
            color = NavText,
            fontWeight = FontWeight.Medium
        )
    }
}