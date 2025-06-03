package com.github.pepitoria.blinkoapp.search.implementation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.pepitoria.blinkoapp.domain.model.note.BlinkoNote
import com.github.pepitoria.blinkoapp.presentation.R
import com.github.pepitoria.blinkoapp.ui.base.ComposableLifecycleEvents
import com.github.pepitoria.blinkoapp.ui.loading.Loading
import com.github.pepitoria.blinkoapp.ui.note.list.NoteListItem
import com.github.pepitoria.blinkoapp.ui.tabbar.TabBar
import com.github.pepitoria.blinkoapp.ui.theme.BlinkoAppTheme
import com.github.pepitoria.blinkoapp.ui.theme.getBackgroundBrush

@Composable
fun SearchScreenInternalComposable(
  viewModel: SearchScreenViewModel = hiltViewModel(),
  noteOnClick: (Int) -> Unit,
  currentRoute: String,
  goToNotes: () -> Unit,
  goToBlinkos: () -> Unit,
  goToSearch: () -> Unit,
  goToSettings: () -> Unit,
  goToTodoList: () -> Unit,
) {
  ComposableLifecycleEvents(viewModel = viewModel)

  val isLoading = viewModel.isLoading.collectAsState()
  val notes = viewModel.notes.collectAsState()
  val query = viewModel.query.collectAsState()

  val onSearch = { querySt: String ->
    viewModel.search(querySt)
  }

  BlinkoAppTheme {
    TabBar(
      currentRoute = currentRoute,
      goToNotes = goToNotes,
      goToBlinkos = goToBlinkos,
      goToTodoList = goToTodoList,
      goToSearch = goToSearch,
      goToSettings = goToSettings,
    ) { paddingValues ->

      SearchScreen(
        isLoading = isLoading.value,
        notes = notes.value,
        query = query.value,
        onSearch = onSearch,
        noteOnClick = noteOnClick,
      )

    }
  }
}

@Composable
private fun SearchScreen(
  isLoading: Boolean,
  notes: List<BlinkoNote>,
  query: String,
  onSearch: (String) -> Unit,
  noteOnClick: (Int) -> Unit = {},
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(
        brush = getBackgroundBrush()
      )
      .padding(16.dp),
  ) {
    SearchBar(
      onSearch = {
        onSearch(it)
      },
      query = query,
    )

    if (isLoading) {
      Loading()
    } else if (notes.isEmpty()) {
      EmptySearch(isSearching = query.isNotEmpty())
    } else {
      SearchResults(
        notes = notes,
        noteOnClick = noteOnClick,
      )
    }
  }
}

@Composable
private fun SearchResults(
  notes: List<BlinkoNote>,
  noteOnClick: (Int) -> Unit = {},
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

@Composable
private fun EmptySearch(
  modifier: Modifier = Modifier,
  isSearching: Boolean = false,
) {
  Spacer(modifier = Modifier.height(8.dp))
  val text = if (isSearching) {
    stringResource(id = R.string.search_no_notes_found)
  } else {
    stringResource(id = R.string.search_make_a_search)
  }

  Text(
    modifier = modifier
      .fillMaxSize(),
    text = text,
    textAlign = TextAlign.Center,
    color = Color.White,
  )
}

@Composable
private fun SearchBar(
  onSearch: (String) -> Unit,
  query: String,
) {
  val focusRequester = remember { FocusRequester() }

  LaunchedEffect(Unit) {
    focusRequester.requestFocus()
  }

  Box {
    Column(
      modifier = Modifier
        .height(64.dp)
    ) {
      TextField(
        label = {
          Text(
            text = "Search...",
            fontWeight = FontWeight.Normal
          )
        },
        value = query,
        singleLine = true,
        onValueChange = {
          onSearch(it)
        },
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Text,
          imeAction = ImeAction.Search
        ),
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(50.dp))
          .focusRequester(focusRequester),
      )

      Spacer(modifier = Modifier.height(8.dp))
    }

    Column(
      modifier = Modifier
        .align(Alignment.CenterEnd)
    ) {
      Icon(
        imageVector = Icons.Default.Search,
        tint = Color.Gray,
        contentDescription = "Buscar",
        modifier = Modifier
          .padding(end = 8.dp)
      )
      Spacer(modifier = Modifier.height(8.dp))
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenPreview() {
  BlinkoAppTheme {
    SearchScreen(
      isLoading = false,
      notes = emptyList(),
      query = "",
      onSearch = {},
    )
  }
}