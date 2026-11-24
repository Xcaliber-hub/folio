package com.folio.typewriter.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.folio.typewriter.data.Note
import com.folio.typewriter.nip.RollerBar
import com.folio.typewriter.ui.editor.paperTexture
import com.folio.typewriter.ui.icons.FolioIcons
import com.folio.typewriter.ui.theme.Ink
import com.folio.typewriter.ui.theme.TypewriterText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.platform.LocalDensity

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    notes: List<Note>,
    onOpen: (Long) -> Unit,
    onNew: () -> Unit,
    onDelete: (Long) -> Unit,
    contentPadding: PaddingValues,
) {
    var confirm by remember { mutableStateOf<Note?>(null) }
    val paper = MaterialTheme.colorScheme.background

    Box(Modifier.fillMaxSize().paperTexture(paper, Ink, seed = 3).padding(contentPadding)) {
        if (notes.isEmpty()) {
            EmptyBox(onNew)
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item(span = { GridItemSpan(2) }) { Header(subtitle = "${notes.size} page${if (notes.size == 1) "" else "s"} in the box.") }
                items(notes, key = { it.id }) { note ->
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = note.paper.color),
                        modifier = Modifier
                            .heightIn(min = 150.dp)
                            .combinedClickable(onClick = { onOpen(note.id) }, onLongClick = { confirm = note }),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(note.updated)).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Ink.copy(alpha = 0.5f),
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                note.text.trim(),
                                style = TypewriterText.copy(fontSize = TypewriterText.fontSize * 0.78f, lineHeight = TypewriterText.lineHeight * 0.8f),
                                color = note.ribbon.color,
                                maxLines = 6,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
            FloatingActionButton(
                onClick = onNew,
                shape = MaterialShapes.Cookie9Sided.toShape(),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp).size(72.dp),
            ) { Icon(FolioIcons.Add, "New page") }
        }
    }

    confirm?.let { n ->
        AlertDialog(
            onDismissRequest = { confirm = null },
            title = { Text("Shred this page?") },
            text = { Text("\"${n.title}\" will be gone for good.") },
            confirmButton = { TextButton(onClick = { onDelete(n.id); confirm = null }) { Text("Shred") } },
            dismissButton = { TextButton(onClick = { confirm = null }) { Text("Keep") } },
        )
    }
}

@Composable
private fun Header(subtitle: String) {
    Column(Modifier.padding(top = 24.dp, bottom = 12.dp)) {
        Text("BOX", style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(6.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Empty state from the reference: the roller waits mid-screen with a + above the nip. */
@Composable
private fun EmptyBox(onNew: () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        Column(Modifier.padding(horizontal = 16.dp)) { Header("A blank page is waiting.") }
        Column(
            Modifier.fillMaxWidth().align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier
                    .size(56.dp)
                    .clickable(onClick = onNew),
                contentAlignment = Alignment.Center,
            ) { Icon(FolioIcons.Add, "New page", tint = MaterialTheme.colorScheme.onBackground) }
            RollerBar(
                carriageX = (widthPx - with(density) { 12.dp.toPx() }) / 2f,
                ink = Ink,
                paper = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp).height(40.dp),
            )
        }
    }
}
