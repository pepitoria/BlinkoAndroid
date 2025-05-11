package com.github.pepitoria.blinkoapp.search.implementation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.pepitoria.blinkoapp.search.api.SearchFactory
import timber.log.Timber
import javax.inject.Inject

class SearchFactoryImpl @Inject constructor(
) : SearchFactory {

  @Composable
  override fun SearchScreenComposable(
    noteOnClick: (Int) -> Unit,
    currentRoute: String,
    goToNotes: () -> Unit,
    goToBlinkos: () -> Unit,
    goToSearch: () -> Unit,
    goToSettings: () -> Unit,
  ) {
    SearchScreenInternalComposable(
      noteOnClick = noteOnClick,
      currentRoute = currentRoute,
      goToNotes = goToNotes,
      goToBlinkos = goToBlinkos,
      goToSearch = goToSearch,
      goToSettings = goToSettings,
    )
  }

}