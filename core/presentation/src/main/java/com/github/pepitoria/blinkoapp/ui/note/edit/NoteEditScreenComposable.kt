package com.github.pepitoria.blinkoapp.ui.note.edit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.pepitoria.blinkoapp.presentation.R
import com.github.pepitoria.blinkoapp.domain.model.note.BlinkoNote
import com.github.pepitoria.blinkoapp.ui.base.ComposableLifecycleEvents
import com.github.pepitoria.blinkoapp.ui.loading.Loading
import com.github.pepitoria.blinkoapp.ui.theme.BlinkoAppTheme

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

  BlinkoAppTheme {
    if (isLoading.value) {
      Loading()
    } else {
      BlinkoNoteEditor(
        uiState = uiState.value,
        modifier = Modifier.fillMaxWidth(),
        updateNote = { viewModel.updateLocalNote(it.content) },
        sendToBlinko = { viewModel.editNote() },
        goBack = { goBack() },
      )
    }
  }
}

@Composable
fun BlinkoNoteEditor(
  uiState: BlinkoNote,
  modifier: Modifier = Modifier,
  updateNote: (BlinkoNote) -> Unit = {},
  sendToBlinko: () -> Unit = {},
  goBack: () -> Unit = {},
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(16.dp)
  ) {
    TextField(
      value = uiState.content,
      onValueChange = { updateNote(uiState.copy(content = it)) },
      label = { Text(text = stringResource(R.string.note_edit_label)) },
      minLines = 3,
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.TopStart)
        .padding(bottom = 64.dp)
    )

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.BottomCenter)
    ) {
      CancelButton(
        modifier = Modifier.weight(1f),
        onClick = goBack
      )
      SaveButton(
        modifier = Modifier.weight(1f),
        onClick = sendToBlinko
      )
    }
  }
}

@Composable
private fun SaveButton(
  modifier: Modifier = Modifier,
  onClick: () -> Unit = {},
) {
  GenericButton(
    modifier = modifier,
    onClick = onClick,
    text = stringResource(R.string.note_edit_save_button_text),
  )
}

@Composable
private fun CancelButton(
  modifier: Modifier = Modifier,
  onClick: () -> Unit = {},
) {
  GenericButton(
    modifier = modifier,
    onClick = onClick,
    text = stringResource(R.string.note_edit_cancel_button_text),
  )
}

@Composable
private fun GenericButton(
  modifier: Modifier = Modifier,
  onClick: () -> Unit = {},
  text: String,
) {
  Button(
    onClick = onClick,
    modifier = modifier
      .padding(8.dp)
  ) {
    Text(
      text = text,
      fontSize = 16.sp
    )
  }
}