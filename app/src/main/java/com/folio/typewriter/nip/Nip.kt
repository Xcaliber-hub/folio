package com.folio.typewriter.nip

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The typewriter nip: rod + carriage pill (three dots) + I-beam type guide.
 * Every shape here is parsed from [NipGeometry], the same path data the
 * launcher icon is generated from, so the logo and the in-app nip are identical.
 */
private object NipPaths {
    private fun parse(d: String, evenOdd: Boolean = false): Path =
        PathParser().parsePathString(d).toPath().apply {
            if (evenOdd) fillType = PathFillType.EvenOdd
        }

    val rodLeft = parse(NipGeometry.ROD_LEFT)
    val rodRight = parse(NipGeometry.ROD_RIGHT)
    val pillSolid = parse(NipGeometry.PILL_SOLID)
    val pill = parse(NipGeometry.PILL, evenOdd = true)
    val ibeam = parse(NipGeometry.IBEAM)
}

/** Draws the carriage (pill + dots + I-beam) with its centre at [centerX], top at [top]. */
fun DrawScope.drawCarriage(centerX: Float, top: Float, unit: Float, ink: Color, paper: Color) {
    translate(left = centerX - NipGeometry.CARRIAGE_CENTER_X * unit, top = top) {
        scale(unit, unit, pivot = Offset.Zero) {
            // Backfill so the dot holes show the page, never the rod behind them.
            drawPath(NipPaths.pillSolid, paper)
            drawPath(NipPaths.pill, ink)
            drawPath(NipPaths.ibeam, ink)
        }
    }
}

/** The full mark exactly as in the logo (short rod + carriage), fitted to the canvas. */
fun DrawScope.drawNipMark(ink: Color, paper: Color) {
    val unit = minOf(size.width / NipGeometry.WIDTH, size.height / NipGeometry.HEIGHT)
    val left = (size.width - NipGeometry.WIDTH * unit) / 2f
    val top = (size.height - NipGeometry.HEIGHT * unit) / 2f
    translate(left, top) {
        scale(unit, unit, pivot = Offset.Zero) {
            drawPath(NipPaths.rodLeft, ink)
            drawPath(NipPaths.rodRight, ink)
            drawPath(NipPaths.pillSolid, paper)
            drawPath(NipPaths.pill, ink)
            drawPath(NipPaths.ibeam, ink)
        }
    }
}

/**
 * The platen rod across the full width, with the carriage riding on it.
 * Rod thickness, end caps and carriage all use the logo's proportions.
 * Height of this composable = [NipGeometry.HEIGHT] units.
 */
@Composable
fun RollerBar(
    carriageX: Float,
    ink: Color,
    paper: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier) {
        val unit = size.height / NipGeometry.HEIGHT
        val rodTop = (NipGeometry.ROD_CENTER_Y - NipGeometry.ROD_THICKNESS / 2f) * unit
        val capTop = (NipGeometry.ROD_CENTER_Y - NipGeometry.CAP_HEIGHT / 2f) * unit
        val capW = NipGeometry.CAP_WIDTH * unit
        val capH = NipGeometry.CAP_HEIGHT * unit
        val capR = CornerRadius(NipGeometry.CAP_RADIUS * unit)
        // rod
        drawRect(ink, Offset(capW / 2f, rodTop), Size(size.width - capW, NipGeometry.ROD_THICKNESS * unit))
        // end caps
        drawRoundRect(ink, Offset(0f, capTop), Size(capW, capH), capR)
        drawRoundRect(ink, Offset(size.width - capW, capTop), Size(capW, capH), capR)
        // carriage, kept inside the caps
        val half = (NipGeometry.CARRIAGE_RIGHT - NipGeometry.CARRIAGE_LEFT) / 2f * unit
        val x = carriageX.coerceIn(capW + half, size.width - capW - half)
        drawCarriage(x, 0f, unit, ink, paper)
    }
}

/** In-app logo: the M3E Cookie9Sided shape in black with the white nip, as on the launcher. */
@Composable
fun FolioLogo(modifier: Modifier = Modifier, size: Dp = 96.dp, inverted: Boolean = false) {
    val fill = if (inverted) Color.White else Color.Black
    val mark = if (inverted) Color.Black else Color.White
    Box(
        modifier
            .size(size)
            .background(fill, MaterialShapes.Cookie9Sided.toShape()),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(size * (56f / 76f), size * (56f / 76f) * NipGeometry.HEIGHT / NipGeometry.WIDTH)) {
            drawNipMark(ink = mark, paper = fill)
        }
    }
}

@Composable
fun NipMark(ink: Color, paper: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) { drawNipMark(ink, paper) }
}
