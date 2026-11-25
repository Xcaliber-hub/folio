package com.folio.typewriter.ui.editor

import android.content.Intent
import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.folio.typewriter.data.MAX_CHARS
import com.folio.typewriter.data.Note
import com.folio.typewriter.data.Paper
import com.folio.typewriter.data.Ribbon
import com.folio.typewriter.data.Settings
import com.folio.typewriter.nip.RollerBar
import com.folio.typewriter.ui.icons.FolioIcons
import com.folio.typewriter.ui.theme.Bone
import com.folio.typewriter.ui.theme.Ink
import com.folio.typewriter.ui.theme.LampGlow
import com.folio.typewriter.ui.theme.LampNight
import com.folio.typewriter.ui.theme.TypewriterText
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToInt

private val FirstLines = listOf(
    "The letter I never sent began like this:",
    "Today smelled like",
    "Three things I want to remember:",
    "If I'm honest,",
    "Dear future me,",
    "It started with a knock at the door.",
)

private val SheetMargin = 14.dp
private val SheetPadH = 26.dp
private val SheetPadTop = 36.dp
private val PromptSlot = 44.dp
private val RollerHeight = 40.dp
private val RollerInset = 6.dp

@Composable
fun EditorScreen(
    initial: Note,
    settings: Settings,
    lamp: Boolean,
    onLampChange: (Boolean) -> Unit,
    onSave: (Note) -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val density = LocalDensity.current
    val keyboard = LocalSoftwareKeyboardController.current

    var value by remember { mutableStateOf(TextFieldValue(initial.text, TextRange(initial.text.length))) }
    var struck by remember { mutableStateOf(initial.struck) }
    var ribbon by remember { mutableStateOf(initial.ribbon) }
    var paper by remember { mutableStateOf(initial.paper) }
    var noDelete by remember { mutableStateOf(settings.noDeleteByDefault) }
    var sound by remember { mutableStateOf(settings.sound) }
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    var prompt by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var lastDingLine by remember { mutableIntStateOf(-1) }
    // Every keystroke bumps this; the carriage hops on the y axis in response.
    var keyTick by remember { mutableIntStateOf(0) }
    val bob = remember { Animatable(0f) }
    LaunchedEffect(keyTick) {
        if (keyTick == 0) return@LaunchedEffect
        val lift = with(density) { 4.dp.toPx() }
        bob.animateTo(-lift, tween(durationMillis = 45))
        bob.animateTo(0f, spring(dampingRatio = 0.32f, stiffness = 900f))
    }

    val sounds = remember { TypewriterSounds(context) }
    DisposableEffect(Unit) { onDispose { sounds.release() } }

    fun current() = initial.copy(
        text = value.text, struck = struck, ribbon = ribbon, paper = paper,
        updated = System.currentTimeMillis(),
    )

    fun strike() {
        keyTick++
        if (settings.haptics) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        if (sound) sounds.key()
    }

    fun onChange(new: TextFieldValue) {
        val old = value
        if (new.text == old.text) { value = new; return }
        if (noDelete && new.text.length < old.text.length) {
            // Drafting mode: backspace overtypes an "x" instead of erasing.
            val idx = (old.selection.start - 1 downTo 0).firstOrNull { it !in struck && old.text[it] != '\n' }
            if (idx != null) { struck = struck + idx; strike() }
            value = old.copy(selection = old.selection)
            return
        }
        if (new.text.length > MAX_CHARS && new.text.length > old.text.length) {
            view.performHapticFeedback(HapticFeedbackConstants.REJECT)
            return
        }
        val prefix = old.text.commonPrefixWith(new.text).length
        val delta = new.text.length - old.text.length
        struck = struck.mapNotNull { i ->
            when {
                i < prefix -> i
                delta < 0 && i < prefix - delta -> null
                else -> i + delta
            }
        }.filter { it in new.text.indices }.toSet()
        if (delta > 0 && '\n' in new.text.substring(prefix, prefix + delta)) {
            if (settings.haptics) view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            if (sound) sounds.carriageReturn()
            keyTick++
        } else {
            strike()
        }
        value = new
    }

    // Autosave a beat after typing stops.
    LaunchedEffect(value.text, struck, ribbon, paper) {
        delay(600)
        onSave(current())
    }
    LaunchedEffect(value.text) { dragOffset = 0f }

    val close = { onSave(current()); onClose() }
    BackHandler { close() }

    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }

    val ink = ribbon.color
    val nipInk = if (lamp) Bone else Ink
    val nipPaper = if (lamp) LampNight else paper.color
    val measurer = rememberTextMeasurer(cacheSize = 128)
    val glyphCache = remember { mutableMapOf<Char, TextLayoutResult>() }

    Box(
        Modifier
            .fillMaxSize()
            .background(if (lamp) LampNight else paper.color.darken(0.05f))
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // ---- top bar ----
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = close) { Icon(FolioIcons.Back, "Back") }
                Spacer(Modifier.weight(1f))
                Text(
                    "${NumberFormat.getIntegerInstance().format(value.text.length)} / 3,000",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                IconButton(onClick = {
                    val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, value.text)
                    context.startActivity(Intent.createChooser(send, null))
                }) { Icon(FolioIcons.Share, "Share") }
            }

            // ---- the machine ----
            BoxWithConstraints(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clipToBounds()
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { d -> dragOffset = (dragOffset + d).coerceIn(0f, 20_000f) },
                    )
                    .clickable(interactionSource = null, indication = null) {
                        runCatching { focus.requestFocus() }
                        keyboard?.show()
                    }
            ) {
                val px = { d: androidx.compose.ui.unit.Dp -> with(density) { d.toPx() } }
                val rollerTop = constraints.maxHeight * 0.58f
                val sheetMinHeight = constraints.maxHeight * 1.6f
                // The layout can lag one frame behind the text (it is produced after this
                // composition), so clamp every offset to the text the layout actually holds.
                val caret: Rect = layout?.let { l ->
                    l.getCursorRect(value.selection.start.coerceIn(0, l.layoutInput.text.length))
                } ?: Rect(0f, 0f, 0f, with(density) { TypewriterText.lineHeight.toPx() })
                val lineBottomInSheet = px(SheetPadTop) + px(PromptSlot) + caret.bottom
                val target = rollerTop - lineBottomInSheet - px(2.dp) + dragOffset
                val sheetY by animateFloatAsState(target, MaterialTheme.motionScheme.defaultSpatialSpec(), label = "feed")
                val caretX = px(SheetMargin) + px(SheetPadH) + caret.left
                val carriageX by animateFloatAsState(caretX, MaterialTheme.motionScheme.fastSpatialSpec(), label = "carriage")
                // Where the page starts curling back over the platen: the typing line and the
                // one above it stay flat, everything higher tips away from you in 3D.
                val lineH = with(density) { TypewriterText.lineHeight.toPx() }
                val foldScreenY = rollerTop - lineH * 2.4f
                val sheetLayer = rememberGraphicsLayer()
                // Drag the carriage sideways: the caret follows it along the current line,
                // ticking once per character like a real carriage.
                fun moveCarriageTo(machineX: Float) {
                    val l = layout ?: return
                    val len = l.layoutInput.text.length
                    val line = l.getLineForOffset(value.selection.start.coerceIn(0, len))
                    val y = (l.getLineTop(line) + l.getLineBottom(line)) / 2f
                    val x = machineX - px(SheetMargin) - px(SheetPadH)
                    val off = l.getOffsetForPosition(Offset(x, y)).coerceIn(0, minOf(len, value.text.length))
                    if (off != value.selection.start || value.selection.length != 0) {
                        value = value.copy(selection = TextRange(off))
                        if (settings.haptics) view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    }
                }
                val foldLayer = rememberGraphicsLayer()

                // Ding near the right margin, once per line.
                LaunchedEffect(layout, value.selection) {
                    val l = layout ?: return@LaunchedEffect
                    val line = l.getLineForOffset(value.selection.start.coerceIn(0, l.layoutInput.text.length))
                    if (caret.left > l.size.width * 0.86f && line != lastDingLine) {
                        lastDingLine = line
                        if (sound) sounds.ding()
                    }
                }

                // Paper sheet, with the desk lamp drawn over it when it's on.
                Box(
                    Modifier
                        .fillMaxSize()
                        .drawWithContent {
                            drawContent()
                            if (lamp) drawLamp(Offset(carriageX, rollerTop + px(8.dp) + bob.value), spread = 0.62f, neck = px(14.dp))
                        }
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(Alignment.Top, unbounded = true)
                            .offset { IntOffset(0, sheetY.roundToInt()) }
                            .drawWithContent {
                                val fold = (foldScreenY - sheetY).coerceIn(0f, size.height)
                                sheetLayer.record { this@drawWithContent.drawContent() }
                                // Flat part: from the fold down.
                                clipRect(top = fold) { drawLayer(sheetLayer) }
                                // Curled part: hinged on the fold line and tipped back.
                                foldLayer.pivotOffset = Offset(size.width / 2f, fold)
                                foldLayer.rotationX = FoldAngle
                                foldLayer.cameraDistance = 6f // smaller = stronger perspective
                                foldLayer.record {
                                    clipRect(bottom = fold) {
                                        drawLayer(sheetLayer)
                                        // Light falls off as the paper turns away.
                                        val reach = px(260.dp)
                                        drawRect(
                                            Brush.verticalGradient(
                                                0f to Color.Black.copy(alpha = 0.34f),
                                                0.75f to Color.Black.copy(alpha = 0.06f),
                                                1f to Color.Transparent,
                                                startY = fold - reach, endY = fold,
                                            ),
                                            topLeft = Offset(0f, fold - reach * 3f),
                                            size = androidx.compose.ui.geometry.Size(size.width, reach * 3f),
                                        )
                                    }
                                }
                                drawLayer(foldLayer)
                                // A soft crease shadow just under the bend.
                                drawRect(
                                    Brush.verticalGradient(
                                        0f to Color.Black.copy(alpha = 0.10f), 1f to Color.Transparent,
                                        startY = fold, endY = fold + px(10.dp),
                                    ),
                                    topLeft = Offset(0f, fold),
                                    size = androidx.compose.ui.geometry.Size(size.width, px(10.dp)),
                                )
                            }
                            .padding(horizontal = SheetMargin)
                            .heightIn(min = with(density) { sheetMinHeight.toDp() })
                            .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                            .paperTexture(paper.color, Ink)
                            .padding(horizontal = SheetPadH)
                            .padding(top = SheetPadTop, bottom = 120.dp)
                    ) {
                        Box(Modifier.height(PromptSlot).fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                            if (value.text.isEmpty()) {
                                TextButton(onClick = { prompt = FirstLines.filterNot { it == prompt }.random() }) {
                                    Text("Need a first line?", color = Ink.copy(alpha = 0.7f), style = MaterialTheme.typography.labelLarge)
                                }
                            } else {
                                Text(
                                    SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(initial.created)).uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Ink.copy(alpha = 0.45f),
                                )
                            }
                        }
                        BasicTextField(
                            value = value,
                            onValueChange = ::onChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focus)
                                .drawBehind {
                                    layout?.let { drawInk(it, value.text, struck, measurer, TypewriterText, ink, glyphCache) }
                                },
                            textStyle = TypewriterText.copy(color = Color.Transparent),
                            cursorBrush = SolidColor(Color.Transparent),
                            onTextLayout = { layout = it },
                            decorationBox = { inner ->
                                Box {
                                    if (value.text.isEmpty()) {
                                        Text(prompt ?: "Begin here.", style = TypewriterText, color = Ink.copy(alpha = 0.38f))
                                    }
                                    inner()
                                }
                            },
                        )
                    }
                }

                // The rod and carriage: same shapes as the logo.
                RollerBar(
                    carriageX = carriageX - px(RollerInset),
                    carriageLift = bob.value,
                    ink = nipInk,
                    paper = nipPaper,
                    modifier = Modifier
                        .offset { IntOffset(0, rollerTop.roundToInt()) }
                        .padding(horizontal = RollerInset)
                        .fillMaxWidth()
                        .height(RollerHeight)
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { change, _ ->
                                change.consume()
                                moveCarriageTo(change.position.x + px(RollerInset))
                            }
                        },
                )
            }

            // ---- toolbar above the keyboard ----
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ToolToggle(FolioIcons.Lamp, "Desk lamp", lamp) { onLampChange(it) }
                    ToolToggle(if (sound) FolioIcons.Sound else FolioIcons.Mute, "Key sounds", sound) { sound = it }
                    ToolToggle(FolioIcons.Strike, "No-delete drafting", noDelete) { noDelete = it }
                    IconButton(onClick = {
                        val stamp = "— " + SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date()) + " —\n"
                        val t = value.text
                        val at = value.selection.start.coerceIn(0, t.length)
                        if (t.length + stamp.length <= MAX_CHARS) {
                            onChange(TextFieldValue(t.substring(0, at) + stamp + t.substring(at), TextRange(at + stamp.length)))
                        }
                    }) { Icon(FolioIcons.Stamp, "Date stamp") }
                    // Ribbon colour: a dot of ink.
                    IconButton(onClick = { ribbon = Ribbon.entries[(ribbon.ordinal + 1) % Ribbon.entries.size] }) {
                        Box(Modifier.size(18.dp).background(ribbon.color, CircleShape))
                    }
                    // Paper stock: an M3E cookie swatch.
                    IconButton(onClick = { paper = Paper.entries[(paper.ordinal + 1) % Paper.entries.size] }) {
                        Box(
                            Modifier
                                .size(22.dp)
                                .background(paper.color, MaterialShapes.Cookie9Sided.toShape())
                                .border(1.dp, Ink.copy(alpha = 0.35f), MaterialShapes.Cookie9Sided.toShape())
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolToggle(icon: ImageVector, label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    IconToggleButton(checked = checked, onCheckedChange = onChange) {
        Box(
            Modifier
                .size(36.dp)
                .background(
                    if (checked) MaterialTheme.colorScheme.primary else Color.Transparent,
                    MaterialShapes.Cookie9Sided.toShape(),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon, label,
                tint = if (checked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/** How far the page above the typing line tips back, in degrees. */
private const val FoldAngle = 42f

/**
 * Desk lamp: the room goes dark except for a beam of warm light rising from the nip.
 * The edge of the beam is a soft penumbra (stacked clips), the light pools on the page
 * as an ellipse the way a real cone lands on a tilted sheet, and the bulb itself glows.
 * Plain clips and gradient fills only: no offscreen layers or blend modes.
 */
private fun DrawScope.drawLamp(apex: Offset, spread: Float, neck: Float) {
    val dark = Color(0xFF0B0809)
    val h = apex.y + 60f
    fun cone(s: Float) = Path().apply {
        val n = neck * s
        val w = n + h * spread * s
        moveTo(apex.x - n, apex.y)
        lineTo(apex.x - w, apex.y - h)
        lineTo(apex.x + w, apex.y - h)
        lineTo(apex.x + n, apex.y)
        close()
    }
    // 1. The dark room, with a soft edge around the beam.
    val steps = 9
    val stepAlpha = 1f - 0.12f.pow(1f / steps)
    for (i in 0 until steps) {
        val s = 0.74f + 0.56f * i / (steps - 1)
        clipPath(cone(s), ClipOp.Difference) { drawRect(dark.copy(alpha = stepAlpha)) }
    }
    // 2. Falloff with distance from the bulb.
    clipPath(cone(1.3f)) {
        drawRect(
            Brush.radialGradient(
                0f to Color.Transparent,
                0.45f to dark.copy(alpha = 0.18f),
                1f to dark.copy(alpha = 0.8f),
                center = apex, radius = h * 0.95f,
            )
        )
    }
    // 3. Warm air in the beam, feathered over three nested cones.
    for (s in floatArrayOf(0.8f, 0.95f, 1.1f)) {
        clipPath(cone(s)) {
            drawRect(
                Brush.radialGradient(
                    0f to LampGlow.copy(alpha = 0.12f),
                    1f to Color.Transparent,
                    center = apex, radius = h * 0.8f,
                )
            )
        }
    }
    // 4. The pool of light where the beam lands on the page.
    val poolY = apex.y - h * 0.32f
    val poolR = neck + h * 0.32f * spread
    val poolCenter = Offset(apex.x, poolY)
    scale(scaleX = 1f, scaleY = 0.55f, pivot = poolCenter) {
        drawCircle(
            Brush.radialGradient(
                0f to LampGlow.copy(alpha = 0.26f),
                0.6f to LampGlow.copy(alpha = 0.08f),
                1f to Color.Transparent,
                center = poolCenter, radius = poolR,
            ),
            radius = poolR, center = poolCenter,
        )
    }
    // 5. The bulb: a hot core with a halo.
    val bulbR = neck * 2.6f
    drawCircle(
        Brush.radialGradient(
            0f to Color(0xFFFFF6E6).copy(alpha = 0.95f),
            0.25f to LampGlow.copy(alpha = 0.55f),
            1f to Color.Transparent,
            center = apex, radius = bulbR,
        ),
        radius = bulbR, center = apex,
    )
}

private fun Color.darken(f: Float) = Color(red * (1 - f), green * (1 - f), blue * (1 - f), alpha)
