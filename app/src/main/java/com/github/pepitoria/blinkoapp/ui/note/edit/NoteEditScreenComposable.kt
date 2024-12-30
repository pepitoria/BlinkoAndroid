package com.github.pepitoria.blinkoapp.ui.note.edit

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun NoteEditScreenComposable(
//  viewModel: NoteListScreenViewModel = hiltViewModel(),
  noteId: Int,
) {
  Text("editing note $noteId")
}