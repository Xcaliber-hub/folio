package com.folio.typewriter.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
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
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                    val colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        indicatorColor = MaterialTheme.colorScheme.primary,
                    )
                    NavigationBarItem(
                        selected = screen == "home", onClick = { screen = "home" },
                        icon = { Icon(FolioIcons.Home, null) }, label = { Text("Home") }, colors = colors,
                    )
                    NavigationBarItem(
                        selected = screen == "studio", onClick = { screen = "studio" },
                        icon = { Icon(FolioIcons.Studio, null) }, label = { Text("Studio") }, colors = colors,
                    )
                }
            },
        ) { padding ->
            AnimatedContent(
                targetState = screen,
                transitionSpec = { (fadeIn() + scaleIn(initialScale = 0.96f)) togetherWith fadeOut() },
                label = "tabs",
            ) { s ->
                when (s) {
                    "studio" -> StudioScreen(settings, { vm.updateSettings(it) }, padding)
                    else -> HomeScreen(
                        notes = notes,
                        onOpen = { lamp = settings.lampByDefault; editingId = it },
                        onNew = { lamp = settings.lampByDefault; editingId = System.currentTimeMillis() },
                        onDelete = { vm.delete(it) },
                        contentPadding = padding,
                    )
                }
            }
        }
    }
}
