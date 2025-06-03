package com.github.pepitoria.blinkoapp.tags

import androidx.compose.runtime.Composable
import com.github.pepitoria.blinkoapp.tags.api.TagsFactory
import javax.inject.Inject

class TagsFactoryImpl @Inject constructor(): TagsFactory {

  @Composable
  override fun TagListComposable() {
    TagListComposableInternal()
  }

}