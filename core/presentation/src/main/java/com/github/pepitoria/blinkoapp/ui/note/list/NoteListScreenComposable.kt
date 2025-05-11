package com.github.pepitoria.blinkoapp.ui.note.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.pepitoria.blinkoapp.R
import com.github.pepitoria.blinkoapp.domain.model.note.BlinkoNote
import com.github.pepitoria.blinkoapp.domain.model.note.BlinkoNoteType
import com.github.pepitoria.blinkoapp.search.api.di.SearchEntryPoint
import com.github.pepitoria.blinkoapp.ui.base.ComposableLifecycleEvents
import com.github.pepitoria.blinkoapp.ui.loading.Loading
import com.github.pepitoria.blinkoapp.ui.tabbar.TabBar
import com.github.pepitoria.blinkoapp.ui.theme.BlinkoAppTheme
import com.github.pepitoria.blinkoapp.ui.theme.getBackgroundBrush
import com.halilibo.richtext.commonmark.Markdown
import com.halilibo.richtext.ui.BasicRichText
import dagger.hilt.android.EntryPointAccessors

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
) {

  PullToRefreshBox(
    isRefreshing = isLoading,
    onRefresh = onRefresh,
    modifier = Modifier
      .fillMaxSize()
      .background(getBackgroundBrush())
      .padding(16.dp)
  ) {
    LazyColumn {
      items(notes) { note ->
        NoteListItem(
          note = note,
          onClick = noteOnClick
        )
        Spacer(modifier = Modifier.height(8.dp))
      }
    }
  }
}

@Composable
fun NoteListItem(
  note: BlinkoNote,
  onClick: (Int) -> Unit = { _ -> }
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