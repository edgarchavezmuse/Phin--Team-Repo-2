package com.example.phinui.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.phinui.ui.theme.HeaderRed
import com.example.phinui.ui.theme.HeaderText

@Composable
fun TopHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(145.dp)
            .background(HeaderRed),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Channel Islands",
                color = HeaderText,
                fontSize = 46.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "CSU",
                color = HeaderText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Thin
            )
        }
    }
}