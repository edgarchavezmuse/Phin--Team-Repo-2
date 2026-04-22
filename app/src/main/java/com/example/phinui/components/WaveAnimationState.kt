package com.example.phinui.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.roundToInt

data class WaveAnimationState(
    val frontPhase: Float,
    val backPhase: Float,
    val shipProgress: Float,
    val bobPhase: Float
)

@Composable
fun rememberWaveAnimationState(
    frontDurationMillis: Int = 1800,
    backDurationMillis: Int = 4000,
    shipSpeedDpPerSecond: Float = 35f,
    bobDurationMillis: Int = 1800
): WaveAnimationState {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val shipSpeedPxPerSecond = with(density) { shipSpeedDpPerSecond.dp.toPx() }

    val travelDistancePx = screenWidthPx * 1.3f

    val shipDurationMillis = ((travelDistancePx / shipSpeedPxPerSecond) * 1000f)
        .roundToInt()
        .coerceAtLeast(1000)

    val transition = rememberInfiniteTransition(label = "shared_wave_animation")

    val frontPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = frontDurationMillis,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "front_wave_phase"
    )

    val backPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = backDurationMillis,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "back_wave_phase"
    )

    val shipProgress by transition.animateFloat(
        initialValue = -0.15f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = shipDurationMillis,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "ship_progress"
    )

    val bobPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = bobDurationMillis,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "ship_bob_phase"
    )

    return WaveAnimationState(
        frontPhase = frontPhase,
        backPhase = backPhase,
        shipProgress = shipProgress,
        bobPhase = bobPhase
    )
}