package com.github.pepitoria.blinkoapp.ui.note.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.pepitoria.blinkoapp.domain.model.note.BlinkoNote

@Composable
fun NoteEditScreenComposable(
//  viewModel: NoteListScreenViewModel = hiltViewModel(),
  noteId: Int,
) {
//  Text("editing note $noteId")
  BlinkoNoteEditor(
    uiState = BlinkoNote.EMPTY,
    modifier = Modifier.fillMaxWidth(),
    updateNote = {},
    sendToBlinko = {}
  )
}

@Composable
fun BlinkoNoteEditor(
  uiState: BlinkoNote,
  modifier: Modifier = Modifier,
  updateNote: (BlinkoNote) -> Unit = {},
  sendToBlinko: () -> Unit = {},
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