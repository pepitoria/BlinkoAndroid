package com.github.pepitoria.blinkoapp.ui.note.edit

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.pepitoria.blinkoapp.ui.notelist.NoteListScreenViewModel

@Composable
fun NoteEditScreenComposable(
//  viewModel: NoteListScreenViewModel = hiltViewModel(),
  noteId: Int,
) {
  Text("editing note $noteId")
}