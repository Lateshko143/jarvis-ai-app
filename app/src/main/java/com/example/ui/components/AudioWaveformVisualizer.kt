package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.core.voice.VoiceEngineState
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisEmerald

@Composable
fun AudioWaveformVisualizer(
    state: VoiceEngineState,
    audioRms: Float = 0f,
    modifier: Modifier = Modifier
) {
    val isActive = state is VoiceEngineState.Listening || state is VoiceEngineState.Speaking
    val color = when (state) {
        is VoiceEngineState.Listening -> JarvisCyan
        is VoiceEngineState.Speaking -> JarvisEmerald
        is VoiceEngineState.Processing -> JarvisAmber
        else -> Color.Gray.copy(alpha = 0.3f)
    }

    val transition = rememberInfiniteTransition(label = "Waveform")
    val barCount = 18

    Row(
        modifier = modifier.height(32.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val animDuration = 350 + (i * 35) % 400
            val animatedHeight by transition.animateFloat(
                initialValue = 4f,
                targetValue = if (isActive) (12f + ((i * 7) % 20)) else 4f,
                animationSpec = infiniteRepeatable(
                    animation = tween(animDuration, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$i"
            )

            val rmsBoost = if (state is VoiceEngineState.Listening) audioRms * 24f else 0f
            val computedHeight = (if (isActive) (animatedHeight + rmsBoost) else 4f).coerceIn(4f, 32f)

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(computedHeight.dp)
                    .background(
                        color = color.copy(alpha = if (isActive) 0.9f else 0.4f),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}
