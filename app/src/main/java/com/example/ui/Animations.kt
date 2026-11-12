package com.example.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisElectricBlue
import com.example.ui.theme.JarvisFocusedBorder
import com.example.ui.theme.JarvisNeonGlow
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated Arc Reactor Holographic HUD Core.
 * Draws concentric rotating rings, glowing energy arcs, and pulsing circles.
 */
@Composable
fun ArcReactorCore(
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    isActive: Boolean = true,
    rmsLevel: Float = 0f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ArcReactorTransitions")

    // Slow clockwise rotation for outer ring
    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outerRotation"
    )

    // Counter-clockwise rotation for middle segmented ring
    val middleRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "middleRotation"
    )

    // Fast clockwise rotation for inner data ring
    val innerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "innerRotation"
    )

    // Pulsing core glow alpha
    val corePulse by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "corePulse"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = this.size.minDimension / 2f - 8.dp.toPx()

            // 1. Outer boundary circle with dashed sci-fi pattern
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 8f, 4f, 8f), outerRotation)
            drawCircle(
                color = JarvisElectricBlue.copy(alpha = 0.45f),
                radius = baseRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx(), pathEffect = dashEffect)
            )

            // 2. Outer segmented arc brackets
            val bracketRadius = baseRadius * 0.88f
            val bracketCount = 4
            for (i in 0 until bracketCount) {
                val startAngle = outerRotation + (i * 90f) + 10f
                drawArc(
                    color = JarvisCyan.copy(alpha = 0.85f),
                    startAngle = startAngle,
                    sweepAngle = 70f,
                    useCenter = false,
                    topLeft = Offset(center.x - bracketRadius, center.y - bracketRadius),
                    size = androidx.compose.ui.geometry.Size(bracketRadius * 2, bracketRadius * 2),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // 3. Middle segmented ring (rotating counter-clockwise)
            val midRadius = baseRadius * 0.72f
            val segmentCount = 12
            for (i in 0 until segmentCount) {
                val startAngle = middleRotation + (i * 30f)
                val alpha = if (i % 2 == 0) 0.9f else 0.4f
                drawArc(
                    color = JarvisNeonGlow.copy(alpha = alpha),
                    startAngle = startAngle,
                    sweepAngle = 20f,
                    useCenter = false,
                    topLeft = Offset(center.x - midRadius, center.y - midRadius),
                    size = androidx.compose.ui.geometry.Size(midRadius * 2, midRadius * 2),
                    style = Stroke(width = 4.dp.toPx())
                )
            }

            // 4. Tick marks between middle and inner ring
            val tickRadius = baseRadius * 0.54f
            val tickCount = 24
            for (i in 0 until tickCount) {
                val angleRad = Math.toRadians((innerRotation + (i * 15f)).toDouble())
                val p1 = Offset(
                    x = (center.x + (tickRadius - 4.dp.toPx()) * cos(angleRad)).toFloat(),
                    y = (center.y + (tickRadius - 4.dp.toPx()) * sin(angleRad)).toFloat()
                )
                val p2 = Offset(
                    x = (center.x + tickRadius * cos(angleRad)).toFloat(),
                    y = (center.y + tickRadius * sin(angleRad)).toFloat()
                )
                drawLine(
                    color = JarvisCyan.copy(alpha = 0.7f),
                    start = p1,
                    end = p2,
                    strokeWidth = 1.5.dp.toPx()
                )
            }

            // 5. Audio-reactive ring (expands when listening/speaking)
            val audioRadius = (baseRadius * 0.42f) + (rmsLevel.coerceIn(0f, 10f) * 2.5f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        JarvisCyan.copy(alpha = if (isActive) corePulse * 0.8f else 0.3f),
                        JarvisElectricBlue.copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = audioRadius + 12.dp.toPx()
                ),
                radius = audioRadius,
                center = center
            )

            // 6. Central Glowing Arc Reactor Core
            val coreRadius = baseRadius * 0.26f
            drawCircle(
                color = JarvisCyan.copy(alpha = if (isActive) corePulse else 0.4f),
                radius = coreRadius,
                center = center
            )

            // 7. Core bright center dot
            drawCircle(
                color = Color.White.copy(alpha = if (isActive) 0.95f else 0.5f),
                radius = coreRadius * 0.45f,
                center = center
            )
        }
    }
}

/**
 * Audio Reactive Waveform visualizer for J.A.R.V.I.S. voice listening and response.
 */
@Composable
fun AudioWaveformVisualizer(
    modifier: Modifier = Modifier,
    barCount: Int = 18,
    isListening: Boolean = false,
    rmsLevel: Float = 0f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveformAnim")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wavePulse"
    )

    Canvas(modifier = modifier) {
        val totalWidth = size.width
        val barWidth = (totalWidth / barCount) * 0.65f
        val spacing = (totalWidth / barCount) * 0.35f
        val centerY = size.height / 2f

        for (i in 0 until barCount) {
            val factor = sin((i.toDouble() / barCount) * Math.PI).toFloat()
            val dynamicHeight = if (isListening) {
                val waveFactor = sin(i + pulse * 6f).coerceAtLeast(0.1f)
                (size.height * 0.3f + (rmsLevel.coerceIn(0f, 10f) * 6f * factor * waveFactor))
                    .coerceIn(4.dp.toPx(), size.height)
            } else {
                size.height * 0.15f * factor
            }

            val x = i * (barWidth + spacing) + spacing / 2f
            val top = centerY - dynamicHeight / 2f
            val bottom = centerY + dynamicHeight / 2f

            val brush = Brush.verticalGradient(
                colors = listOf(
                    JarvisNeonGlow,
                    JarvisCyan,
                    JarvisElectricBlue
                ),
                startY = top,
                endY = bottom
            )

            drawRoundRect(
                brush = brush,
                topLeft = Offset(x, top),
                size = androidx.compose.ui.geometry.Size(barWidth, dynamicHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}

/**
 * Modifier for Android TV D-Pad Focus Glow and Scale animation.
 */
@Composable
fun Modifier.tvFocusGlow(
    isFocused: Boolean,
    cornerRadius: Dp = 12.dp
): Modifier {
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.06f else 1.0f,
        animationSpec = tween(durationMillis = 200),
        label = "tvScale"
    )

    return this
        .scale(scale)
        .border(
            width = if (isFocused) 2.5.dp else 1.dp,
            brush = if (isFocused) {
                Brush.horizontalGradient(
                    listOf(JarvisFocusedBorder, JarvisNeonGlow, JarvisCyan)
                )
            } else {
                Brush.horizontalGradient(
                    listOf(Color(0xFF163E63), Color(0xFF0F2E4A))
                )
            },
            shape = RoundedCornerShape(cornerRadius)
        )
}
