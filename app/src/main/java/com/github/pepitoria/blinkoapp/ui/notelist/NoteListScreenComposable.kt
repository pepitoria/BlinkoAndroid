package com.github.pepitoria.blinkoapp.ui.notelist

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.pepitoria.blinkoapp.domain.model.note.BlinkoNote
import com.github.pepitoria.blinkoapp.ui.base.ComposableLifecycleEvents
import com.github.pepitoria.blinkoapp.ui.loading.Loading
import com.github.pepitoria.blinkoapp.ui.theme.BlinkoAppTheme

@Composable
fun NoteListScreenComposable(
  viewModel: NoteListScreenViewModel = hiltViewModel(),
) {
  ComposableLifecycleEvents(viewModel = viewModel)

  val isLoading = viewModel.isLoading.collectAsState()
  val notes = viewModel.notes.collectAsState()

  BlinkoAppTheme {
    if (isLoading.value) {
      Loading()
    } else if (notes.value.isEmpty()) {
      EmptyNoteList()
    } else {
      NoteList(notes = notes.value)
    }
  }
}

@Composable
private fun EmptyNoteList() {
  Text("No notes found")
}

@Composable
private fun NoteList(notes: List<BlinkoNote>) {

  Text("Notes list")

  LazyColumn {
    items(notes) { note ->
      Text(note.content)
    }
  }
}