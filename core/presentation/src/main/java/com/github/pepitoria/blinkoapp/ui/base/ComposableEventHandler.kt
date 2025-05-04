package com.github.pepitoria.blinkoapp.ui.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

@Composable
fun ComposableLifecycleEvents(viewModel: ViewModelComposableEvents) {
  DisposableEffect(key1 = viewModel) {
    viewModel.onStart()
    onDispose { viewModel.onStop() }
  }
}