package com.github.pepitoria.blinkoapp.notes.implementation.presentation

import androidx.compose.runtime.Composable
import com.github.pepitoria.blinkoapp.notes.api.NotesFactory
import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNoteType
import javax.inject.Inject

class NotesFactoryImpl @Inject constructor() : NotesFactory {

  @Composable
  override fun NoteListScreenComposable(
    noteOnClick: (Int) -> Unit,
    noteType: BlinkoNoteType,
    currentRoute: String,
    goToNotes: () -> Unit,
    goToBlinkos: () -> Unit,
    goToSearch: () -> Unit,
    goToSettings: () -> Unit,
    goToNewNote: () -> Unit,
    goToTodoList: () -> Unit,
  ) {
    NoteListScreenComposableInternal(
      noteOnClick = noteOnClick,
      noteType = noteType,
      currentRoute = currentRoute,
      goToNotes = goToNotes,
      goToBlinkos = goToBlinkos,
      goToSearch = goToSearch,
      goToSettings = goToSettings,
      goToNewNote = goToNewNote,
      goToTodoList = goToTodoList,
    )
  }

  @Composable
  override fun NoteEditScreenComposable(
    noteId: Int,
    goBack: () -> Unit,
  ) {
    NoteEditScreenComposableInternal(
      noteId = noteId,
      goBack = goBack,
    )
  }
}
