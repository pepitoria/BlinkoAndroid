package com.github.pepitoria.blinkoapp.tags.presentation

import androidx.compose.runtime.Composable
import com.github.pepitoria.blinkoapp.tags.api.TagsFactory
import javax.inject.Inject

class TagsFactoryImpl @Inject constructor() : TagsFactory {

  @Composable
  override fun TagListComposable(onTagClick: (String) -> Unit) {
    TagListComposableInternal(
      onTagClick = onTagClick,
    )
  }
}
