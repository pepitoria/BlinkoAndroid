package com.github.pepitoria.blinkoapp.notes.api

import androidx.compose.runtime.Composable
import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNoteType

interface NotesFactory {

  @Composable
  fun NoteListScreenComposable(
    noteOnClick: (Int) -> Unit,
    noteType: BlinkoNoteType,
    currentRoute: String,
    goToNotes: () -> Unit,
    goToBlinkos: () -> Unit,
    goToSearch: () -> Unit,
    goToSettings: () -> Unit,
    goToNewNote: () -> Unit,
    goToTodoList: () -> Unit,
  )

  @Composable
  fun NoteEditScreenComposable(
    noteId: Int,
    goBack: () -> Unit,
  )
}
