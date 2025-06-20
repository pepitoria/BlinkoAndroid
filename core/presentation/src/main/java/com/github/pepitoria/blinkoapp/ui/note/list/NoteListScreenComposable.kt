package com.github.pepitoria.blinkoapp.ui.note.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.pepitoria.blinkoapp.domain.model.note.BlinkoNote
import com.github.pepitoria.blinkoapp.domain.model.note.BlinkoNoteType
import com.github.pepitoria.blinkoapp.presentation.R
import com.github.pepitoria.blinkoapp.ui.base.ComposableLifecycleEvents
import com.github.pepitoria.blinkoapp.ui.loading.Loading
import com.github.pepitoria.blinkoapp.ui.tabbar.TabBar
import com.github.pepitoria.blinkoapp.ui.theme.BlinkoAppTheme
import com.github.pepitoria.blinkoapp.ui.theme.getBackgroundBrush
import com.halilibo.richtext.commonmark.Markdown
import com.halilibo.richtext.ui.BasicRichText
import timber.log.Timber

@Composable
fun NoteListScreenComposable(
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
          onClick = goToNewNote
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
          noteOnClick = noteOnClick,
          isLoading = isLoading.value,
          onRefresh = { viewModel.refresh() },
          onDeleteSwipe = { note ->
            viewModel.deleteNote(note)
          },
        )
      }
    }
  }
}

@Composable
fun AddNoteFAB(onClick: () -> Unit) {
  FloatingActionButton(
    onClick = { onClick() }
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
  noteOnClick: (Int) -> Unit = {},
  isLoading: Boolean = false,
  onRefresh: () -> Unit = {},
  onDeleteSwipe: (BlinkoNote) -> Unit = { _ -> }
) {

  PullToRefreshBox(
    isRefreshing = isLoading,
    onRefresh = onRefresh,
    modifier = Modifier
      .fillMaxSize()
      .background(getBackgroundBrush())
      .padding(16.dp)
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxSize()
    ) {
      items(
        items = notes,
        key = { it.id ?: 0 }
      ) { note ->
        NoteListItem(
          note = note,
          onClick = noteOnClick,
          onDeleteSwipe = onDeleteSwipe,
        )
        Spacer(modifier = Modifier.height(8.dp))
      }
    }
  }
}

@Composable
fun NoteListItem(
  note: BlinkoNote,
  onClick: (Int) -> Unit = { _ -> },
  onDeleteSwipe: (BlinkoNote) -> Unit = { _ -> }
) {
  val dismissState = rememberSwipeToDismissBoxState(
    positionalThreshold = { totalDistance ->
      totalDistance * 0.5f
    },
    confirmValueChange = { newValue ->
      when (newValue) {
          SwipeToDismissBoxValue.EndToStart -> {
            onDeleteSwipe(note)
            false
          }
          else -> {
            false
          }
      }
    }
  )

  SwipeToDismissBox(
    state = dismissState,
    backgroundContent = {
      Box(
        modifier = Modifier
          .fillMaxSize(),
        contentAlignment = Alignment.CenterEnd
      ) {
        Text(
          text = stringResource(R.string.release_to_delete),
          color = Color.White,
          modifier = Modifier
            .padding(4.dp)
            .align(Alignment.Center)
        )
        Icon(
          imageVector = Icons.Filled.Delete,
          contentDescription = stringResource(id = R.string.delete_note),
          tint = Color.Black,
        )
      }
    },
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth(),
      colors = BlinkoAppTheme.cardColors(),
      onClick = { note.id?.let(onClick) }
    ) {
      BasicRichText(
        modifier = Modifier.padding(16.dp),
      ) {
        Markdown(
          content = note.content.trimIndent(),
        )
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
          type = BlinkoNoteType.BLINKO
        ),
        BlinkoNote(
          id = 2,
          content = "This is another note",
          type = BlinkoNoteType.BLINKO
        )
      )
    )
  }
}