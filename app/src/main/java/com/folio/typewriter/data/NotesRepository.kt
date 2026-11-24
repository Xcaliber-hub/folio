package com.folio.typewriter.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** Notes live in one small JSON file; a page is capped at 3,000 characters so this stays tiny. */
class NotesRepository(context: Context) {
    private val file = File(context.filesDir, "notes.json")
    private val _notes = MutableStateFlow(load())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    fun get(id: Long): Note? = _notes.value.firstOrNull { it.id == id }

    suspend fun upsert(note: Note) {
        _notes.update { list ->
            val rest = list.filterNot { it.id == note.id }
            if (note.text.isBlank()) rest else (listOf(note) + rest).sortedByDescending { it.updated }
        }
        persist()
    }

    suspend fun delete(id: Long) {
        _notes.update { list -> list.filterNot { it.id == id } }
        persist()
    }

    private suspend fun persist() = withContext(Dispatchers.IO) {
        val arr = JSONArray()
        _notes.value.forEach { n ->
            arr.put(
                JSONObject()
                    .put("id", n.id)
                    .put("text", n.text)
                    .put("struck", JSONArray(n.struck.sorted()))
                    .put("ribbon", n.ribbon.name)
                    .put("paper", n.paper.name)
                    .put("created", n.created)
                    .put("updated", n.updated)
            )
        }
        val tmp = File(file.parentFile, "notes.json.tmp")
        tmp.writeText(arr.toString())
        tmp.renameTo(file)
    }

    private fun load(): List<Note> = runCatching {
        if (!file.exists()) return emptyList()
        val arr = JSONArray(file.readText())
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            val s = o.optJSONArray("struck") ?: JSONArray()
            Note(
                id = o.getLong("id"),
                text = o.getString("text"),
                struck = (0 until s.length()).map { s.getInt(it) }.toSet(),
                ribbon = runCatching { Ribbon.valueOf(o.getString("ribbon")) }.getOrDefault(Ribbon.Black),
                paper = runCatching { Paper.valueOf(o.getString("paper")) }.getOrDefault(Paper.Lilac),
                created = o.optLong("created"),
                updated = o.optLong("updated"),
            )
        }.sortedByDescending { it.updated }
    }.getOrDefault(emptyList())
}

class SettingsRepository(context: Context) {
    private val prefs = context.getSharedPreferences("folio_settings", Context.MODE_PRIVATE)
    private val _settings = MutableStateFlow(read())
    val settings: StateFlow<Settings> = _settings.asStateFlow()

    fun update(transform: (Settings) -> Settings) {
        val s = transform(_settings.value)
        _settings.value = s
        prefs.edit()
            .putBoolean("sound", s.sound)
            .putBoolean("haptics", s.haptics)
            .putBoolean("lamp", s.lampByDefault)
            .putBoolean("noDelete", s.noDeleteByDefault)
            .putString("paper", s.paper.name)
            .apply()
    }

    private fun read() = Settings(
        sound = prefs.getBoolean("sound", true),
        haptics = prefs.getBoolean("haptics", true),
        lampByDefault = prefs.getBoolean("lamp", false),
        noDeleteByDefault = prefs.getBoolean("noDelete", false),
        paper = runCatching { Paper.valueOf(prefs.getString("paper", null)!!) }.getOrDefault(Paper.Lilac),
    )
}
