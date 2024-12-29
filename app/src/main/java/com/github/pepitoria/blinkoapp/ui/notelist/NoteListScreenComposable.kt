package com.github.pepitoria.blinkoapp.ui.notelist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.pepitoria.blinkoapp.domain.model.note.BlinkoNote
import com.github.pepitoria.blinkoapp.ui.base.ComposableLifecycleEvents
import com.github.pepitoria.blinkoapp.ui.loading.Loading
import com.github.pepitoria.blinkoapp.ui.theme.BlinkoAppTheme
import com.halilibo.richtext.commonmark.Markdown
import com.halilibo.richtext.ui.BasicRichText

@Composable
fun NoteListScreenComposable(
  viewModel: NoteListScreenViewModel = hiltViewModel(),
) {
  ComposableLifecycleEvents(viewModel = viewModel)

  val isLoading = viewModel.isLoading.collectAsState()
  val notes = viewModel.notes.collectAsState()

  BlinkoAppTheme {
    if (isLoading.value) {
      Loading()
    } else if (notes.value.isEmpty()) {
      EmptyNoteList()
    } else {
      NoteList(notes = notes.value)
    }
  }
}

@Composable
private fun EmptyNoteList() {
  Text("No notes found")
}

@Composable
private fun NoteList(notes: List<BlinkoNote>) {

  Column(
    modifier = Modifier.padding(16.dp)
  ) {
    Text(
      text = "Notes list",
      fontSize = 24.sp
    )
    Spacer(modifier = Modifier.height(8.dp))

    LazyColumn {
      items(notes) { note ->
        NoteListItem(note)
        Spacer(modifier = Modifier.height(8.dp))
      }
    }
  }
}

@Composable
private fun NoteListItem(note: BlinkoNote) {

  Card (
    modifier = Modifier.fillMaxWidth()
  ) {
    BasicRichText(
      modifier = Modifier.padding(16.dp)
    ) {
      Markdown(
        content = note.content.trimIndent()
      )
    }
  }
}