package com.folio.typewriter.ui.editor

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import kotlin.random.Random

/** Paper stock: base colour, fine grain and a scatter of ink specks. Stable per [seed]. */
fun Modifier.paperTexture(color: Color, ink: Color, seed: Int = 7): Modifier = drawWithCache {
    val rnd = Random(seed)
    val area = size.width * size.height
    val grain = List((area / 900f).toInt().coerceAtMost(6000)) {
        Offset(rnd.nextFloat() * size.width, rnd.nextFloat() * size.height)
    }
    val specks = List((area / 26000f).toInt().coerceAtMost(260)) {
        Triple(
            Offset(rnd.nextFloat() * size.width, rnd.nextFloat() * size.height),
            0.6f + rnd.nextFloat() * 1.6f,
            0.18f + rnd.nextFloat() * 0.45f,
        )
    }
    onDrawBehind {
        drawRect(color)
        drawPoints(grain, PointMode.Points, ink.copy(alpha = 0.05f), strokeWidth = density * 1.2f, cap = StrokeCap.Round)
        specks.forEach { (o, r, a) -> drawCircle(ink.copy(alpha = a), radius = r * density, center = o) }
    }
}

/** Deterministic 0..1 noise for character [i] and salt [k]. */
private fun hash01(i: Int, c: Char, k: Int): Float {
    var h = i * 374761393 + c.code * 668265263 + k * 1274126177
    h = (h xor (h ushr 13)) * 1274126177
    h = h xor (h ushr 16)
    return (h and 0xFFFF) / 65535f
}

/**
 * Re-draws the typed text glyph by glyph with typewriter imperfections:
 * uneven ink, a hair of baseline jitter and tilt, and the odd faint double strike.
 * Struck characters get an overtyped "x".
 */
fun DrawScope.drawInk(
    layout: TextLayoutResult,
    text: String,
    struck: Set<Int>,
    measurer: TextMeasurer,
    style: TextStyle,
    ink: Color,
    glyphCache: MutableMap<Char, TextLayoutResult>,
) {
    fun glyph(c: Char) = glyphCache.getOrPut(c) { measurer.measure(AnnotatedString(c.toString()), style) }
    val jitter = density * 0.6f
    for (i in text.indices) {
        val c = text[i]
        if (c == '\n' || c == ' ') continue
        if (i >= layout.layoutInput.text.length) break
        val box = layout.getBoundingBox(i)
        val g = glyph(c)
        val alpha = 0.72f + hash01(i, c, 1) * 0.28f
        val dx = (hash01(i, c, 2) - 0.5f) * jitter
        val dy = (hash01(i, c, 3) - 0.5f) * jitter * 1.4f
        val tilt = (hash01(i, c, 4) - 0.5f) * 3f
        val topLeft = Offset(box.left + dx, box.top + dy)
        rotate(tilt, pivot = box.center) {
            drawText(g, color = ink.copy(alpha = alpha), topLeft = topLeft)
            if (hash01(i, c, 5) > 0.93f) {
                drawText(g, color = ink.copy(alpha = 0.22f), topLeft = topLeft + Offset(density * 0.7f, -density * 0.3f))
            }
        }
        if (i in struck) {
            drawText(glyph('x'), color = ink.copy(alpha = 0.9f), topLeft = Offset(box.left, box.top))
        }
    }
}
