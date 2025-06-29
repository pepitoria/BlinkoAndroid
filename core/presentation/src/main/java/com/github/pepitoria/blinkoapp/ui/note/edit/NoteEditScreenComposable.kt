package com.github.pepitoria.blinkoapp.ui.note.edit

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.pepitoria.blinkoapp.domain.model.note.BlinkoNote
import com.github.pepitoria.blinkoapp.domain.model.note.BlinkoNoteType
import com.github.pepitoria.blinkoapp.presentation.R
import com.github.pepitoria.blinkoapp.ui.base.ComposableLifecycleEvents
import com.github.pepitoria.blinkoapp.ui.loading.Loading
import com.github.pepitoria.blinkoapp.ui.theme.BlinkoAppTheme
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun NoteEditScreenComposable(
  viewModel: NoteEditScreenViewModel = hiltViewModel(),
  noteId: Int,
  goBack: () -> Unit,
) {
  ComposableLifecycleEvents(viewModel = viewModel)

  val isLoading = viewModel.isLoading.collectAsState()
  val uiState = viewModel.noteUiModel.collectAsState()
  val noteTypes = viewModel.noteTypes.collectAsState()

  viewModel.onStart(noteId = noteId, onNoteUpsert = goBack)
  ListenForErrors(viewModel.error)

  BlinkoAppTheme {
    if (isLoading.value) {
      Loading()
    } else {
      BlinkoNoteEditor(
        uiState = uiState.value,
        noteTypes = noteTypes.value,
        modifier = Modifier.fillMaxWidth(),
        defaultNoteType = uiState.value.type,
        updateNote = {
          viewModel.updateLocalNote(
            content = it.content,
            noteType = it.type.value
          )
        },
        sendToBlinko = { viewModel.upsertNote() },
        goBack = { goBack() },
      )
    }
  }
}

@Composable
private fun ListenForErrors(
  errors: SharedFlow<String?>,
  ) {
  val context = LocalContext.current
  LaunchedEffect(Unit) {
    errors.collect { error ->
      error?.let {
        Toast.makeText(
          context,
          context.getString(R.string.error_toast, it),
          Toast.LENGTH_LONG).show()
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun BlinkoNoteEditorPreview() {
  BlinkoNoteEditor(
    uiState = BlinkoNote(
      id = 1,
      content = "This is a sample note content for preview purposes.",
      type = BlinkoNoteType.BLINKO,
      isArchived = false,
    ),
    noteTypes = listOf(
      BlinkoNoteType.BLINKO.value,
      BlinkoNoteType.NOTE.value,
      BlinkoNoteType.TODO.value
    ),
    defaultNoteType = BlinkoNoteType.BLINKO,
    updateNote = {},
    sendToBlinko = {},
    goBack = {}
  )
}

@Composable
fun BlinkoNoteEditor(
  uiState: BlinkoNote,
  noteTypes: List<Int>,
  defaultNoteType: BlinkoNoteType,
  modifier: Modifier = Modifier,
  updateNote: (BlinkoNote) -> Unit,
  sendToBlinko: () -> Unit,
  goBack: () -> Unit,
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
        .padding(bottom = 128.dp)
    )

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.BottomCenter)
    ) {

      val noteType = if (uiState.id == -1) {
        defaultNoteType
      } else {
        uiState.type
      }

      BlinkoDropDown(
        items = noteTypes,
        selectedItem = noteType.value,
        onItemSelected = {
          updateNote(uiState.copy(type = BlinkoNoteType.fromResponseType(it)))
        }
      )

      Row(
        modifier = Modifier
          .fillMaxWidth()
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
}

@Composable
fun BlinkoDropDown(
  items: List<Int>,
  selectedItem: Int,
  onItemSelected: (Int) -> Unit
) {
  var expanded by remember { mutableStateOf(false) }

  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
    ) {
      NoteIcon(
        selection = items[selectedItem],
        modifier = Modifier
        .align(Alignment.CenterVertically)
      )
      NoteText(
        selection = selectedItem,
        modifier = Modifier
          .padding(8.dp)
          .align(Alignment.CenterVertically)
          .clickable { expanded = true },
      )
    }

    DropdownMenu(
      modifier = Modifier
        .padding(8.dp)
        .fillMaxWidth(),
      expanded = expanded,
      onDismissRequest = { expanded = false }
    ) {
      items.forEach { item ->
        DropdownMenuItem(
          leadingIcon = {
            NoteIcon(selection = item)
          },
          text = {
            NoteText(item, modifier = Modifier.padding(4.dp).align(Alignment.CenterHorizontally))
          },
          onClick = {
            onItemSelected(item)
            expanded = false
          }
        )
      }
    }
  }
}

@Composable
private fun NoteIcon(selection: Int, modifier: Modifier = Modifier) {
  when (selection) {
    0 -> Icon(ImageVector.vectorResource(id = R.drawable.blinko), contentDescription = stringResource(R.string.tab_bar_blinkos), modifier = modifier)
    1 -> Icon(ImageVector.vectorResource(id = R.drawable.note), contentDescription = stringResource(R.string.tab_bar_notes), modifier = modifier)
    2 -> Icon(ImageVector.vectorResource(id = R.drawable.todo), contentDescription = stringResource(R.string.tab_bar_todos), modifier = modifier)
    else -> Icon(ImageVector.vectorResource(id = R.drawable.blinko), contentDescription = stringResource(R.string.tab_bar_blinkos), modifier = modifier)
  }
}

@Composable
private fun NoteText(selection: Int, modifier: Modifier = Modifier) {
  val array = LocalContext.current.resources.getStringArray(R.array.blinko_note_types)
  Text(text = array[selection], modifier = modifier.fillMaxWidth())
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