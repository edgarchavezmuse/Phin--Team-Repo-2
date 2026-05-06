package com.example.phinui.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class WaveShip(
    val progress: Float,
    val label: String? = null,
    val size: Dp = 28.dp,
    val verticalOffset: Float = 33f
)

@Composable
fun AnimatedWaves(
    modifier: Modifier = Modifier,
    waveColor: Color,
    height: Dp = 110.dp,
    frontAlpha: Float = 1f,
    backAlpha: Float = 0.5f,
    frontAmplitudeFraction: Float = 0.12f,
    backAmplitudeFraction: Float = 0.08f,
    frontBaseHeightFraction: Float = 0.55f,
    backBaseHeightFraction: Float = 0.45f,
    frontWavelengthFraction: Float = 0.9f,
    backWavelengthFraction: Float = 1.2f,
    showShip: Boolean = false,
    shipColor: Color = MaterialTheme.colorScheme.onTertiary,
    shipSize: Dp = 28.dp,
    shipVerticalOffset: Float = 33f,
    ships: List<WaveShip> = emptyList(),
    frontPhase: Float,
    backPhase: Float,
    shipProgress: Float,
    bobPhase: Float
) {
    val labelColor = MaterialTheme.colorScheme.onBackground

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        fun waveY(
            x: Float,
            baseHeightFraction: Float,
            amplitudeFraction: Float,
            wavelengthFraction: Float,
            phase: Float
        ): Float {
            return size.height * baseHeightFraction +
                    (size.height * amplitudeFraction) * sin(
                ((2f * PI.toFloat() * x) / (size.width * wavelengthFraction)) + phase
            )
        }

        fun wavePath(
            baseHeightFraction: Float,
            amplitudeFraction: Float,
            wavelengthFraction: Float,
            phase: Float
        ): Path {
            return Path().apply {
                moveTo(0f, size.height)

                for (x in 0..size.width.toInt() step 8) {
                    val xf = x.toFloat()
                    val y = waveY(
                        x = xf,
                        baseHeightFraction = baseHeightFraction,
                        amplitudeFraction = amplitudeFraction,
                        wavelengthFraction = wavelengthFraction,
                        phase = phase
                    )
                    lineTo(xf, y)
                }

                lineTo(size.width, size.height)
                close()
            }
        }

        drawPath(
            path = wavePath(
                baseHeightFraction = backBaseHeightFraction,
                amplitudeFraction = backAmplitudeFraction,
                wavelengthFraction = backWavelengthFraction,
                phase = backPhase
            ),
            color = waveColor.copy(alpha = backAlpha)
        )

        drawPath(
            path = wavePath(
                baseHeightFraction = frontBaseHeightFraction,
                amplitudeFraction = frontAmplitudeFraction,
                wavelengthFraction = frontWavelengthFraction,
                phase = frontPhase
            ),
            color = waveColor.copy(alpha = frontAlpha)
        )

        val shipsToDraw =
            if (ships.isNotEmpty()) {
                ships
            } else if (showShip) {
                listOf(
                    WaveShip(
                        progress = shipProgress,
                        size = shipSize,
                        verticalOffset = shipVerticalOffset
                    )
                )
            } else {
                emptyList()
            }

        shipsToDraw.forEach { ship ->
            val shipWidth = ship.size.toPx() * 1.8f
            val shipHeight = ship.size.toPx()
            val shipX = size.width * ship.progress

            if (shipX < -120f || shipX > size.width + 120f) return@forEach

            val frontWaveY = waveY(
                x = shipX,
                baseHeightFraction = frontBaseHeightFraction,
                amplitudeFraction = frontAmplitudeFraction,
                wavelengthFraction = frontWavelengthFraction,
                phase = frontPhase
            )

            // calculate wave slope
            val slope = cos(
                ((2f * PI.toFloat() * shipX) / (size.width * frontWavelengthFraction)) + frontPhase
            )

            // smaller bob so the ship still feels alive
            val bobOffset = size.height * 0.005f * sin(bobPhase)

            // target waterline position, ship vert, position
            val shipY = frontWaveY - shipHeight * 0.50f + bobOffset + ship.verticalOffset

            // tilt based on wave slope
            val tilt = slope * 10f

            ship.label?.let { label ->
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    shipX - shipWidth * 0.45f,
                    shipY - shipHeight * 1.15f,
                    Paint().apply {
                        color = android.graphics.Color.argb(
                            (labelColor.alpha * 255).toInt(),
                            (labelColor.red * 255).toInt(),
                            (labelColor.green * 255).toInt(),
                            (labelColor.blue * 255).toInt()
                        )
                        textSize = 34f
                        isAntiAlias = true
                        textAlign = Paint.Align.LEFT
                    }
                )
            }

            rotate(
                degrees = tilt,
                pivot = Offset(shipX, shipY)
            ) {
                val hull = Path().apply {
                    moveTo(shipX - shipWidth * 0.45f, shipY)
                    lineTo(shipX + shipWidth * 0.35f, shipY)
                    lineTo(shipX + shipWidth * 0.22f, shipY + shipHeight * 0.28f)
                    lineTo(shipX - shipWidth * 0.32f, shipY + shipHeight * 0.28f)
                    close()
                }
                drawPath(hull, color = shipColor)

                drawLine(
                    color = shipColor,
                    start = Offset(shipX - shipWidth * 0.05f, shipY),
                    end = Offset(shipX - shipWidth * 0.05f, shipY - shipHeight * 0.8f),
                    strokeWidth = shipWidth * 0.04f
                )

                val sail = Path().apply {
                    moveTo(shipX - shipWidth * 0.03f, shipY - shipHeight * 0.75f)
                    lineTo(shipX - shipWidth * 0.03f, shipY - shipHeight * 0.1f)
                    lineTo(shipX + shipWidth * 0.3f, shipY - shipHeight * 0.38f)
                    close()
                }
                drawPath(
                    path = sail,
                    color = shipColor.copy(alpha = 0.9f)
                )
            }
        }
    }
}