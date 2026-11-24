package com.folio.typewriter

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.folio.typewriter.data.Note
import com.folio.typewriter.data.NotesRepository
import com.folio.typewriter.data.Settings
import com.folio.typewriter.data.SettingsRepository
import kotlinx.coroutines.launch

class FolioViewModel(app: Application) : AndroidViewModel(app) {
    private val notesRepo = NotesRepository(app)
    private val settingsRepo = SettingsRepository(app)

    val notes = notesRepo.notes
    val settings = settingsRepo.settings

    fun note(id: Long): Note? = notesRepo.get(id)
    fun save(note: Note) = viewModelScope.launch { notesRepo.upsert(note) }
    fun delete(id: Long) = viewModelScope.launch { notesRepo.delete(id) }
    fun updateSettings(transform: (Settings) -> Settings) = settingsRepo.update(transform)
}
