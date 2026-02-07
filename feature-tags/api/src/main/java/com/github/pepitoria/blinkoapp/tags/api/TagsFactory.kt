package com.github.pepitoria.blinkoapp.tags.api

import androidx.compose.runtime.Composable

interface TagsFactory {

  @Composable
  fun TagListComposable(onTagClick: (String) -> Unit)
}
