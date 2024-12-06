package com.github.pepitoria.blinkoapp.ui.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.DisposableEffectScope

@Composable
fun ComposableLifecycleEvents(viewModel: ViewModelComposableEvents) {
  DisposableEffect(key1 = viewModel) {
    viewModel.onStart()
    onDispose { viewModel.onStop() }
  }
}