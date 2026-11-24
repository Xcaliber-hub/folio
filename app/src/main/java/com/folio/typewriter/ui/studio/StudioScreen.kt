package com.folio.typewriter.ui.studio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.folio.typewriter.data.Paper
import com.folio.typewriter.data.Settings
import com.folio.typewriter.nip.FolioLogo
import com.folio.typewriter.ui.editor.paperTexture
import com.folio.typewriter.ui.theme.Ink

@Composable
fun StudioScreen(
    settings: Settings,
    onChange: ((Settings) -> Settings) -> Unit,
    contentPadding: PaddingValues,
) {
    Column(
        Modifier
            .fillMaxSize()
            .paperTexture(MaterialTheme.colorScheme.background, Ink, seed = 5)
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            FolioLogo(size = 72.dp)
            Spacer(Modifier.size(16.dp))
            Column {
                Text("Studio", style = MaterialTheme.typography.displaySmall)
                Text("Tune the machine.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(24.dp))

        Section("Paper stock") {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Paper.entries.forEach { p ->
                    val selected = settings.paper == p
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier
                                .size(if (selected) 56.dp else 48.dp)
                                .background(p.color, MaterialShapes.Cookie9Sided.toShape())
                                .border(if (selected) 2.dp else 1.dp, Ink.copy(alpha = if (selected) 1f else 0.3f), MaterialShapes.Cookie9Sided.toShape())
                                .clickable { onChange { it.copy(paper = p) } }
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(p.label, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        Section("Feel") {
            Toggle("Key sounds", "Clack, ding and carriage return", settings.sound) { v -> onChange { it.copy(sound = v) } }
            Toggle("Haptics", "A light tap on every keystroke", settings.haptics) { v -> onChange { it.copy(haptics = v) } }
        }

        Section("Writing") {
            Toggle("Desk lamp by default", "Open pages in the dark, under the lamp", settings.lampByDefault) { v -> onChange { it.copy(lampByDefault = v) } }
            Toggle("No-delete drafting", "Backspace types an x over the letter", settings.noDeleteByDefault) { v -> onChange { it.copy(noDeleteByDefault = v) } }
        }
        Spacer(Modifier.height(120.dp))
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Text(title.uppercase(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 8.dp, bottom = 8.dp))
    Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.surfaceContainer) {
        Column { content() }
    }
    Spacer(Modifier.height(20.dp))
}

@Composable
private fun Toggle(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(title, style = MaterialTheme.typography.titleMedium) },
        supportingContent = { Text(subtitle) },
        trailingContent = { Switch(checked = checked, onCheckedChange = onChange) },
        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
        modifier = Modifier.clickable { onChange(!checked) },
    )
}
