package com.github.pepitoria.blinkoapp.auth.api

import androidx.compose.runtime.Composable

interface AuthFactory {

  @Composable
  fun LoginScreenComposable(goToHome: () -> Unit)
}
