package com.example.phinui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.phinui.ui.components.AnimatedWaves
import com.example.phinui.ui.components.WaveShip
import com.example.phinui.ui.components.rememberWaveAnimationState
import com.example.phinui.ui.theme.HeaderRed

@Composable
fun CreditsScreen() {
    val waveAnimation = rememberWaveAnimationState(
        frontDurationMillis = 1800,
        backDurationMillis = 4000,
        shipSpeedDpPerSecond = 35f,
        bobDurationMillis = 2000
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Crew",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Built by the Phin sailing crew",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }

        AnimatedWaves(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            waveColor = HeaderRed,
            height = 180.dp,
            ships = listOf(
                WaveShip(
                    progress = 0.10f,
                    label = "Noa",
                    size = 28.dp,
                    verticalOffset = -30f
                ),
                WaveShip(
                    progress = 0.30f,
                    label = "Maria",
                    size = 28.dp,
                    verticalOffset = -10f
                ),
                WaveShip(
                    progress = 0.50f,
                    label = "David",
                    size = 28.dp,
                    verticalOffset = 10f
                ),
                WaveShip(
                    progress = 0.70f,
                    label = "Cheese",
                    size = 28.dp,
                    verticalOffset = 30f
                ),
                WaveShip(
                    progress = 0.90f,
                    label = "Sam",
                    size = 28.dp,
                    verticalOffset = 50f
                )
            ),
            frontPhase = waveAnimation.frontPhase,
            backPhase = waveAnimation.backPhase,
            shipProgress = 0f,
            bobPhase = waveAnimation.bobPhase
        )
    }
}