package com.folio.typewriter.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.folio.typewriter.R

// ---- Brand: black & white, printed on lilac paper ----
val Ink = Color(0xFF1C1A1F)
val InkSoft = Color(0xFF4A4450)
val Bone = Color(0xFFFFFFFF)
val LampNight = Color(0xFF17131A)
val LampGlow = Color(0xFFFFD6A8)

val SpecialElite = FontFamily(Font(R.font.special_elite))
val CourierPrime = FontFamily(Font(R.font.courier_prime))

/** The typed text on the page. */
val TypewriterText = TextStyle(
    fontFamily = SpecialElite,
    fontSize = 18.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.2.sp,
    // Identical line boxes everywhere, so per-glyph redraws line up with the field.
    lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None),
)

private val base = Typography()
val FolioTypography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = SpecialElite),
    displayMedium = base.displayMedium.copy(fontFamily = SpecialElite),
    displaySmall = base.displaySmall.copy(fontFamily = SpecialElite),
    headlineLarge = base.headlineLarge.copy(fontFamily = SpecialElite),
    headlineMedium = base.headlineMedium.copy(fontFamily = SpecialElite),
    headlineSmall = base.headlineSmall.copy(fontFamily = SpecialElite),
    titleLarge = base.titleLarge.copy(fontFamily = SpecialElite),
    titleMedium = base.titleMedium.copy(fontFamily = CourierPrime),
    titleSmall = base.titleSmall.copy(fontFamily = CourierPrime),
    bodyLarge = base.bodyLarge.copy(fontFamily = CourierPrime),
    bodyMedium = base.bodyMedium.copy(fontFamily = CourierPrime),
    bodySmall = base.bodySmall.copy(fontFamily = CourierPrime),
    labelLarge = base.labelLarge.copy(fontFamily = CourierPrime),
    labelMedium = base.labelMedium.copy(fontFamily = CourierPrime),
    labelSmall = base.labelSmall.copy(fontFamily = CourierPrime),
)

// M3 Expressive leans on big, soft corners.
val FolioShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

private fun paperScheme(paper: Color) = lightColorScheme(
    primary = Ink,
    onPrimary = Bone,
    primaryContainer = Color(0xFF2C2830),
    onPrimaryContainer = Bone,
    secondary = InkSoft,
    onSecondary = Bone,
    secondaryContainer = Ink.copy(alpha = 0.10f).compositeOn(paper),
    onSecondaryContainer = Ink,
    tertiary = Color(0xFFA8322A),
    onTertiary = Bone,
    background = paper,
    onBackground = Ink,
    surface = paper,
    onSurface = Ink,
    onSurfaceVariant = InkSoft,
    surfaceContainerLowest = Bone.copy(alpha = 0.35f).compositeOn(paper),
    surfaceContainerLow = Ink.copy(alpha = 0.03f).compositeOn(paper),
    surfaceContainer = Ink.copy(alpha = 0.06f).compositeOn(paper),
    surfaceContainerHigh = Ink.copy(alpha = 0.09f).compositeOn(paper),
    surfaceContainerHighest = Ink.copy(alpha = 0.12f).compositeOn(paper),
    outline = Ink.copy(alpha = 0.45f).compositeOn(paper),
    outlineVariant = Ink.copy(alpha = 0.18f).compositeOn(paper),
)

private val LampScheme = darkColorScheme(
    primary = Bone,
    onPrimary = Ink,
    primaryContainer = Color(0xFFE9E3EC),
    onPrimaryContainer = Ink,
    secondary = Color(0xFFCFC6D4),
    onSecondary = Ink,
    secondaryContainer = Color(0xFF3A3340),
    onSecondaryContainer = Bone,
    tertiary = LampGlow,
    background = LampNight,
    onBackground = Bone,
    surface = LampNight,
    onSurface = Bone,
    onSurfaceVariant = Color(0xFFCFC6D4),
    surfaceContainerLow = Color(0xFF1F1A22),
    surfaceContainer = Color(0xFF241F28),
    surfaceContainerHigh = Color(0xFF2E2833),
    surfaceContainerHighest = Color(0xFF39323E),
    outline = Color(0xFF8B8291),
    outlineVariant = Color(0xFF4A4350),
)

private fun Color.compositeOn(bg: Color): Color {
    val a = alpha
    return Color(
        red = red * a + bg.red * (1 - a),
        green = green * a + bg.green * (1 - a),
        blue = blue * a + bg.blue * (1 - a),
        alpha = 1f,
    )
}

@Composable
fun FolioTheme(
    paper: Color,
    lamp: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialExpressiveTheme(
        colorScheme = if (lamp) LampScheme else paperScheme(paper),
        motionScheme = MotionScheme.expressive(),
        shapes = FolioShapes,
        typography = FolioTypography,
        content = content,
    )
}
