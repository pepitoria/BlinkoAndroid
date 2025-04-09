package com.github.pepitoria.blinkoapp.ui.note.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.pepitoria.blinkoapp.domain.model.note.BlinkoNote
import com.github.pepitoria.blinkoapp.domain.model.note.BlinkoNoteType
import com.github.pepitoria.blinkoapp.ui.base.ComposableLifecycleEvents
import com.github.pepitoria.blinkoapp.ui.loading.Loading
import com.github.pepitoria.blinkoapp.ui.tabbar.TabBar
import com.github.pepitoria.blinkoapp.ui.theme.BlinkoAppTheme
import com.github.pepitoria.blinkoapp.ui.theme.getBackgroundBrush
import com.halilibo.richtext.commonmark.Markdown
import com.halilibo.richtext.ui.BasicRichText

@Composable
fun NoteListScreenComposable(
  viewModel: NoteListScreenViewModel = hiltViewModel(),
  noteOnClick: (Int) -> Unit,
  noteType: BlinkoNoteType,
  currentRoute: String,
  goToNotes: () -> Unit,
  goToBlinkos: () -> Unit,
  goToSettings: () -> Unit,
  ) {
  ComposableLifecycleEvents(viewModel = viewModel)

  val isLoading = viewModel.isLoading.collectAsState()
  val notes = viewModel.getNotes(noteType).collectAsState()

  BlinkoAppTheme {
    TabBar(
      currentRoute = currentRoute,
      goToNotes = goToNotes,
      goToBlinkos = goToBlinkos,
      goToSettings = goToSettings,
    ) { paddingValues ->
      if (isLoading.value) {
        Loading()
      } else if (notes.value.isEmpty()) {
        EmptyNoteList()
      } else {
        NoteList(
          notes = notes.value,
          noteOnClick = noteOnClick,
        )
      }
    }
  }
}

@Composable
private fun EmptyNoteList() {
  Text("No notes found")
}

@Composable
private fun NoteList(
  notes: List<BlinkoNote>,
  noteOnClick: (Int) -> Unit = {},
) {

  Column(
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
private fun NoteListItem(
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