package com.folio.typewriter.data

import androidx.compose.ui.graphics.Color

const val MAX_CHARS = 3_000

enum class Paper(val label: String, val color: Color) {
    Lilac("Lilac", Color(0xFFEBDDF0)),
    OnionSkin("Onion skin", Color(0xFFF3F0E8)),
    Legal("Legal", Color(0xFFF6EDB5)),
    Kraft("Kraft", Color(0xFFD9BF98)),
}

enum class Ribbon(val label: String, val color: Color) {
    Black("Black", Color(0xFF1E1B20)),
    Red("Red", Color(0xFFA8322A)),
    Blue("Blue", Color(0xFF2A3E78)),
}

data class Note(
    val id: Long,
    val text: String = "",
    /** Indices of characters struck over with an x (no-delete drafting). */
    val struck: Set<Int> = emptySet(),
    val ribbon: Ribbon = Ribbon.Black,
    val paper: Paper = Paper.Lilac,
    val created: Long = System.currentTimeMillis(),
    val updated: Long = System.currentTimeMillis(),
) {
    val title: String
        get() = text.lineSequence().firstOrNull { it.isNotBlank() }?.trim()?.take(40) ?: "Untitled page"
}

data class Settings(
    val sound: Boolean = true,
    val haptics: Boolean = true,
    val lampByDefault: Boolean = false,
    val noDeleteByDefault: Boolean = false,
    val paper: Paper = Paper.Lilac,
)
