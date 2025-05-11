package com.github.pepitoria.blinkoapp.search.api

import androidx.compose.runtime.Composable

interface SearchFactory {

  @Composable
  fun SearchScreenComposable(
    noteOnClick: (Int) -> Unit,
    currentRoute: String,
    goToNotes: () -> Unit,
    goToBlinkos: () -> Unit,
    goToSearch: () -> Unit,
    goToSettings: () -> Unit,
  )
}