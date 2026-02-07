package com.github.pepitoria.blinkoapp.notes.implementation.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNote
import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNoteType
import com.github.pepitoria.blinkoapp.notes.api.presentation.NoteListItem
import com.github.pepitoria.blinkoapp.shared.theme.BlinkoAppTheme
import com.github.pepitoria.blinkoapp.shared.theme.getBackgroundColor
import com.github.pepitoria.blinkoapp.shared.ui.R
import com.github.pepitoria.blinkoapp.shared.ui.base.ComposableLifecycleEvents
import com.github.pepitoria.blinkoapp.shared.ui.loading.Loading
import com.github.pepitoria.blinkoapp.shared.ui.tabbar.TabBar

@Composable
fun NoteListScreenComposableInternal(
  viewModel: NoteListScreenViewModel = hiltViewModel(),
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
  viewModel.setNoteType(noteType)
  ComposableLifecycleEvents(viewModel = viewModel)

  val isLoading = viewModel.isLoading.collectAsState()
  val notes = viewModel.notes.collectAsState()
  val noteType = viewModel.noteType.collectAsState()
  val archived = viewModel.archived.collectAsState()

  BlinkoAppTheme {
    TabBar(
      currentRoute = currentRoute,
      goToNotes = goToNotes,
      goToBlinkos = goToBlinkos,
      goToTodoList = goToTodoList,
      goToSearch = goToSearch,
      goToSettings = goToSettings,
      floatingActionButton = {
        AddNoteFAB(
          onClick = goToNewNote,
        )
      },
    ) { paddingValues ->
      if (isLoading.value) {
        Loading()
      } else if (notes.value.isEmpty()) {
        EmptyNoteList()
      } else {
        NoteList(
          notes = notes.value,
          archived = archived.value,
          noteType = noteType.value,
          noteOnClick = noteOnClick,
          isLoading = isLoading.value,
          onRefresh = { viewModel.refresh() },
          onDeleteSwipe = { note ->
            viewModel.deleteNote(note)
          },
          markAsDone = { note ->
            viewModel.markNoteAsDone(note)
          },
        )
      }
    }
  }
}

@Composable
fun AddNoteFAB(onClick: () -> Unit) {
  FloatingActionButton(
    onClick = { onClick() },
  ) {
    Icon(Icons.Filled.Add, stringResource(id = R.string.note_list_add_note))
  }
}

@Composable
private fun EmptyNoteList() {
  Text(text = stringResource(id = R.string.note_list_no_notes_found))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteList(
  notes: List<BlinkoNote>,
  archived: List<BlinkoNote>,
  noteType: BlinkoNoteType,
  noteOnClick: (Int) -> Unit = {},
  isLoading: Boolean = false,
  onRefresh: () -> Unit = {},
  onDeleteSwipe: (BlinkoNote) -> Unit = { _ -> },
  markAsDone: (BlinkoNote) -> Unit = { _ -> },
) {
  PullToRefreshBox(
    isRefreshing = isLoading,
    onRefresh = onRefresh,
    modifier = Modifier
      .fillMaxSize()
      .background(color = getBackgroundColor())
      .padding(16.dp),
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
    ) {
      items(
        items = notes,
        key = { it.id ?: 0 },
      ) { note ->
        NoteListItem(
          note = note,
          onClick = noteOnClick,
          onDeleteSwipe = onDeleteSwipe,
          markAsDone = markAsDone,
        )
        Spacer(modifier = Modifier.height(8.dp))
      }

      if (noteType == BlinkoNoteType.TODO) {
        item {
          Text(
            text = stringResource(id = R.string.todo_done),
            modifier = Modifier.padding(16.dp),
          )
        }

        items(
          items = archived,
          key = { it.id ?: 0 },
        ) { note ->
          NoteListItem(
            note = note,
            onClick = noteOnClick,
            onDeleteSwipe = onDeleteSwipe,
            markAsDone = markAsDone,
          )
          Spacer(modifier = Modifier.height(8.dp))
        }
      }
    }
  }
}

@Composable
@Preview
private fun NoteListPreview() {
  BlinkoAppTheme {
    NoteList(
      notes = listOf(
        BlinkoNote(
          id = 1,
          content = "This is a note",
          type = BlinkoNoteType.BLINKO,
          isArchived = false,
        ),
        BlinkoNote(
          id = 2,
          content = "This is another note",
          type = BlinkoNoteType.TODO,
          isArchived = false,
        ),
      ),
      noteType = BlinkoNoteType.TODO,
      archived = emptyList(),
    )
  }
}
