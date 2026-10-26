package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.voice.VoiceEngineState
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisBlue
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisEmerald
import com.example.ui.theme.JarvisRedAlert

@Composable
fun HolographicReactor(
    state: VoiceEngineState,
    audioRms: Float = 0f,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ReactorRings")

    val rotationOuter by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OuterRing"
    )

    val rotationInner by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "InnerRing"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CorePulse"
    )

    val activeColor = when (state) {
        is VoiceEngineState.Listening -> JarvisCyan
        is VoiceEngineState.Processing -> JarvisAmber
        is VoiceEngineState.Speaking -> JarvisEmerald
        is VoiceEngineState.Error -> JarvisRedAlert
        is VoiceEngineState.Idle -> JarvisBlue
    }

    // Audio-reactive dynamic scale
    val dynamicAudioPulse = 1f + (audioRms * 0.25f)

    Box(
        modifier = modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = this.center
            val baseRadius = size.minDimension / 2.3f
            val radius = baseRadius * pulseScale * dynamicAudioPulse

            // 1. Outermost Tech Target Border
            drawCircle(
                color = activeColor.copy(alpha = 0.15f),
                radius = baseRadius + 14.dp.toPx(),
                style = Stroke(width = 1.dp.toPx())
            )

            // 2. Outer Segmented Arcs
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(activeColor.copy(alpha = 0.1f), activeColor, activeColor.copy(alpha = 0.1f))
                ),
                startAngle = rotationOuter,
                sweepAngle = 130f,
                useCenter = false,
                style = Stroke(width = 3.5.dp.toPx())
            )

            drawArc(
                brush = Brush.sweepGradient(
                    listOf(activeColor, activeColor.copy(alpha = 0.15f))
                ),
                startAngle = rotationOuter + 180f,
                sweepAngle = 100f,
                useCenter = false,
                style = Stroke(width = 3.5.dp.toPx())
            )

            // 3. Inner Reverse Arcs with Glow
            drawArc(
                color = activeColor.copy(alpha = 0.7f),
                startAngle = rotationInner,
                sweepAngle = 210f,
                useCenter = false,
                style = Stroke(width = 2.dp.toPx())
            )

            // 4. Center Glowing Reactor Core
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        activeColor.copy(alpha = 0.75f),
                        activeColor.copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * 0.65f
                ),
                radius = radius * 0.65f
            )

            // 5. Center Iris Core
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = 8.dp.toPx() * pulseScale
            )
        }

        // Central Status Hologram
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = when (state) {
                    is VoiceEngineState.Listening -> "LISTENING"
                    is VoiceEngineState.Processing -> "THINKING"
                    is VoiceEngineState.Speaking -> "SPEAKING"
                    is VoiceEngineState.Error -> "DISRUPTED"
                    is VoiceEngineState.Idle -> "STANDBY"
                },
                color = activeColor,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "JARVIS CORE",
                color = JarvisCyanLight.copy(alpha = 0.6f),
                fontFamily = FontFamily.Monospace,
                fontSize = 8.sp,
                letterSpacing = 1.sp
            )
        }
    }
}
