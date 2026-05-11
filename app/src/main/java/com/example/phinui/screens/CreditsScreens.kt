package com.example.phinui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.phinui.ui.components.AnimatedWaves
import com.example.phinui.ui.components.WaveShip
import com.example.phinui.ui.components.rememberWaveAnimationState
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

@Composable
fun CreditsScreen() {
    val waveAnimation = rememberWaveAnimationState(
        frontDurationMillis = 2200,
        backDurationMillis = 4200,
        shipSpeedDpPerSecond = 35f,
        bobDurationMillis = 2000
    )

    val isDarkMode = MaterialTheme.colorScheme.surface == Color(0xFF1E1E1E) ||
            MaterialTheme.colorScheme.background == Color(0xFF121212)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        if (isDarkMode) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val stars = listOf(
                    Offset(size.width * 0.10f, size.height * 0.12f),
                    Offset(size.width * 0.22f, size.height * 0.18f),
                    Offset(size.width * 0.35f, size.height * 0.09f),
                    Offset(size.width * 0.48f, size.height * 0.16f),
                    Offset(size.width * 0.62f, size.height * 0.11f),
                    Offset(size.width * 0.74f, size.height * 0.20f),
                    Offset(size.width * 0.86f, size.height * 0.13f),

                    Offset(size.width * 0.15f, size.height * 0.28f),
                    Offset(size.width * 0.29f, size.height * 0.32f),
                    Offset(size.width * 0.44f, size.height * 0.26f),
                    Offset(size.width * 0.58f, size.height * 0.34f),
                    Offset(size.width * 0.76f, size.height * 0.29f),

                    Offset(size.width * 0.20f, size.height * 0.45f),
                    Offset(size.width * 0.50f, size.height * 0.42f),
                    Offset(size.width * 0.80f, size.height * 0.48f)
                )

                stars.forEachIndexed { index, star ->
                    val radius =
                        if (index % 5 == 0) 4f
                        else if (index % 3 == 0) 3f
                        else 2f

                    val alpha =
                        if (index % 4 == 0) 0.9f
                        else 0.6f

                    drawCircle(
                        color = Color.White.copy(alpha = alpha),
                        radius = radius,
                        center = star
                    )
                }

                // Delphinus constellation
                val delphinusStars = listOf(
                    Offset(size.width * 0.36f, size.height * 0.17f), // top left
                    Offset(size.width * 0.54f, size.height * 0.185f), // top right
                    Offset(size.width * 0.60f, size.height * 0.255f), // middle right
                    Offset(size.width * 0.72f, size.height * 0.43f), // lower tail
                    Offset(size.width * 0.43f, size.height * 0.23f)  // center left
                )

                delphinusStars.forEachIndexed { index, star ->
                    drawCircle(
                        color = Color.White.copy(alpha = 0.16f),
                        radius = if (index == 3) 13f else 11f,
                        center = star
                    )

                    drawCircle(
                        color = Color.White.copy(alpha = 0.95f),
                        radius = if (index == 3) 5f else 6f,
                        center = star
                    )
                }
            }
        }

        val cloudColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        if (!isDarkMode) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                fun cloud(cx: Float, cy: Float, scale: Float) {

                    drawCircle(cloudColor, radius = 28f * scale, center = Offset(cx, cy))
                    drawCircle(cloudColor, radius = 36f * scale, center = Offset(cx + 32f * scale, cy - 10f * scale))
                    drawCircle(cloudColor, radius = 26f * scale, center = Offset(cx + 68f * scale, cy))
                    drawCircle(cloudColor, radius = 22f * scale, center = Offset(cx + 100f * scale, cy + 6f * scale))
                }

                cloud(
                    cx = size.width * 0.08f,
                    cy = size.height * 0.16f,
                    scale = 0.75f
                )

                cloud(
                    cx = size.width * 0.58f,
                    cy = size.height * 0.24f,
                    scale = 0.95f
                )

                cloud(
                    cx = size.width * 0.22f,
                    cy = size.height * 0.38f,
                    scale = 0.55f
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Built by the Phin sailing crew",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.7f)
            )
        }

        AnimatedWaves(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            waveColor = MaterialTheme.colorScheme.primary,
            height = 180.dp,
            ships = listOf(
                WaveShip(
                    progress = 0.10f,
                    label = "Sam",
                    size = 28.dp,
                    verticalOffset = 20f
                ),
                WaveShip(
                    progress = 0.30f,
                    label = "Maria",
                    size = 28.dp,
                    verticalOffset = 20f
                ),
                WaveShip(
                    progress = 0.50f,
                    label = "Cheese",
                    size = 28.dp,
                    verticalOffset = 20f
                ),
                WaveShip(
                    progress = 0.70f,
                    label = "David",
                    size = 28.dp,
                    verticalOffset = 20f
                ),
                WaveShip(
                    progress = 0.90f,
                    label = "Noa",
                    size = 28.dp,
                    verticalOffset = 20f
                )
            ),
            frontPhase = waveAnimation.frontPhase,
            backPhase = waveAnimation.backPhase,
            shipProgress = 0f,
            bobPhase = waveAnimation.bobPhase
        )
    }
}