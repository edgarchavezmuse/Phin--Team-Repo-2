package com.example.phinui.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.phinui.ui.theme.Background
import com.example.phinui.ui.theme.NavText

@Composable
fun ScheduleScreen() {
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = "Schedule",
                    fontSize = 28.sp,
                    color = NavText
                )
            }
        }
    }
}