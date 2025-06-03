package com.github.pepitoria.blinkoapp.search.implementation

import androidx.compose.runtime.Composable
import com.github.pepitoria.blinkoapp.search.api.SearchFactory
import com.github.pepitoria.blinkoapp.tags.api.TagsFactory
import javax.inject.Inject

class SearchFactoryImpl @Inject constructor(
  private val tagsFactory: TagsFactory,
) : SearchFactory {

  @Composable
  override fun SearchScreenComposable(
    noteOnClick: (Int) -> Unit,
    currentRoute: String,
    goToNotes: () -> Unit,
    goToBlinkos: () -> Unit,
    goToSearch: () -> Unit,
    goToSettings: () -> Unit,
    goToTodoList: () -> Unit,
  ) {
    SearchScreenInternalComposable(
      noteOnClick = noteOnClick,
      currentRoute = currentRoute,
      goToNotes = goToNotes,
      goToBlinkos = goToBlinkos,
      goToTodoList = goToTodoList,
      goToSearch = goToSearch,
      goToSettings = goToSettings,
      tagsFactory = tagsFactory,
    )
  }

}