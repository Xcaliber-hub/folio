package com.folio.typewriter.ui.glass

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

/** Frosted glass: blurs whatever sits behind it (Android 12+; a tinted scrim on older phones). */
fun Modifier.frostedGlass(state: HazeState, tint: Color, shape: Shape = CircleShape): Modifier = this
    .shadow(14.dp, shape, ambientColor = Color.Black.copy(alpha = 0.25f), spotColor = Color.Black.copy(alpha = 0.3f))
    .clip(shape)
    .hazeEffect(state) {
        backgroundColor = tint
        tints = listOf(HazeTint(tint.copy(alpha = 0.52f)))
        blurRadius = 22.dp
        noiseFactor = 0.12f
    }
    .border(0.75.dp, Color.White.copy(alpha = 0.16f), shape)
