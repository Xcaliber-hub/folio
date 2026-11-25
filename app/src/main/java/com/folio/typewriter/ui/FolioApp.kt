package com.folio.typewriter.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import android.content.Intent
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.folio.typewriter.CrashReporter
import androidx.compose.material3.MaterialTheme
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.Surface
import androidx.compose.material3.toShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.folio.typewriter.FolioViewModel
import com.folio.typewriter.data.Note
import com.folio.typewriter.ui.editor.EditorScreen
import com.folio.typewriter.ui.home.HomeScreen
import com.folio.typewriter.ui.icons.FolioIcons
import com.folio.typewriter.ui.studio.StudioScreen
import com.folio.typewriter.ui.theme.FolioTheme

/** Screens: "home", "studio", or the id of the page being typed. */
@Composable
fun FolioApp(vm: FolioViewModel) {
    val notes by vm.notes.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    var screen by rememberSaveable { mutableStateOf("home") }
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    var lamp by rememberSaveable { mutableStateOf(settings.lampByDefault) }

    // If the last session crashed, show what happened so it can be shared.
    val context = LocalContext.current
    var crash by remember { mutableStateOf(CrashReporter.read(context)) }
    crash?.let { trace ->
        FolioTheme(paper = settings.paper.color) {
            AlertDialog(
                onDismissRequest = { CrashReporter.clear(context); crash = null },
                title = { Text("Folio jammed last time") },
                text = {
                    Text(
                        trace.lineSequence().take(14).joinToString("\n"),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState()),
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, trace)
                        context.startActivity(Intent.createChooser(send, "Share crash report"))
                        CrashReporter.clear(context); crash = null
                    }) { Text("Share report") }
                },
                dismissButton = {
                    TextButton(onClick = { CrashReporter.clear(context); crash = null }) { Text("Dismiss") }
                },
            )
        }
    }

    val id = editingId
    if (id != null) {
        val note = remember(id) { vm.note(id) ?: Note(id = id, paper = settings.paper) }
        FolioTheme(paper = note.paper.color, lamp = lamp) {
            EditorScreen(
                initial = note,
                settings = settings,
                lamp = lamp,
                onLampChange = { lamp = it },
                onSave = { vm.save(it) },
                onClose = { editingId = null },
            )
        }
        return
    }

    FolioTheme(paper = settings.paper.color) {
        // Screens run edge to edge; the two-icon tab pill floats over them.
        val insets = WindowInsets.systemBars.asPaddingValues()
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            AnimatedContent(
                targetState = screen,
                transitionSpec = { (fadeIn() + scaleIn(initialScale = 0.96f)) togetherWith fadeOut() },
                label = "tabs",
            ) { s ->
                when (s) {
                    "studio" -> StudioScreen(settings, { vm.updateSettings(it) }, insets)
                    else -> HomeScreen(
                        notes = notes,
                        onOpen = { lamp = settings.lampByDefault; editingId = it },
                        onNew = { lamp = settings.lampByDefault; editingId = System.currentTimeMillis() },
                        onDelete = { vm.delete(it) },
                        contentPadding = insets,
                    )
                }
            }
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shadowElevation = 12.dp,
                tonalElevation = 3.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 28.dp),
            ) {
                Row(
                    Modifier.padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TabIcon(FolioIcons.Home, "Home", screen == "home") { screen = "home" }
                    TabIcon(FolioIcons.Studio, "Studio", screen == "studio") { screen = "studio" }
                }
            }
        }
    }
}

@Composable
private fun TabIcon(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
        label = "tabBg",
    )
    val fg by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "tabFg",
    )
    Box(
        Modifier
            .size(52.dp)
            .clip(if (selected) MaterialShapes.Cookie9Sided.toShape() else CircleShape)
            .background(bg)
            .clickable(role = Role.Tab, onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = label, tint = fg, modifier = Modifier.size(24.dp))
    }
}
