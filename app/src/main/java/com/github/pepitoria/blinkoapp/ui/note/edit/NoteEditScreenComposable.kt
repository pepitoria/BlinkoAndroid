package com.github.pepitoria.blinkoapp.ui.note.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.pepitoria.blinkoapp.domain.model.note.BlinkoNote
import com.github.pepitoria.blinkoapp.ui.base.ComposableLifecycleEvents
import com.github.pepitoria.blinkoapp.ui.loading.Loading

@Composable
fun NoteEditScreenComposable(
  viewModel: NoteEditScreenViewModel = hiltViewModel(),
  noteId: Int,
  goBack: () -> Unit,
) {
  ComposableLifecycleEvents(viewModel = viewModel)

  val isLoading = viewModel.isLoading.collectAsState()
  val uiState = viewModel.noteUiModel.collectAsState()

  viewModel.onStart(noteId = noteId, onNoteUpsert = goBack)

  if (isLoading.value) {
    Loading()
  } else {
    BlinkoNoteEditor(
      uiState = uiState.value,
      modifier = Modifier.fillMaxWidth(),
      updateNote = { viewModel.updateLocalNote(it.content) },
      sendToBlinko = { viewModel.editNote() },
      onNoteUpsert = goBack
    )
  }
}

@Composable
fun BlinkoNoteEditor(
  uiState: BlinkoNote,
  modifier: Modifier = Modifier,
  updateNote: (BlinkoNote) -> Unit = {},
  sendToBlinko: () -> Unit = {},
  onNoteUpsert: () -> Unit = {},
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(16.dp)
  ) {
    TextField(
      value = uiState.content,
      onValueChange = { updateNote(uiState.copy(content = it)) },
      label = { Text("Content") },
      minLines = 3,
      modifier = Modifier.fillMaxWidth()
    )

    Button(
      onClick = sendToBlinko,
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Text(
        text = "Send to Blinko",
        fontSize = 16.sp
      )
    }

  }
}