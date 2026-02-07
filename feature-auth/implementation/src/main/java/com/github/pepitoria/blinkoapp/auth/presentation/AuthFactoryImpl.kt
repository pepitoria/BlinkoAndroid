package com.github.pepitoria.blinkoapp.auth.presentation

import androidx.compose.runtime.Composable
import com.github.pepitoria.blinkoapp.auth.api.AuthFactory
import javax.inject.Inject

class AuthFactoryImpl @Inject constructor() : AuthFactory {

  @Composable
  override fun LoginScreenComposable(goToHome: () -> Unit) {
    LoginWidget(
      goToHome = goToHome,
    )
  }
}
