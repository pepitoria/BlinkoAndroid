package com.github.pepitoria.blinkoapp.settings.presentation

import androidx.compose.runtime.Composable
import com.github.pepitoria.blinkoapp.settings.api.SettingsFactory
import javax.inject.Inject

class SettingsFactoryImpl @Inject constructor() : SettingsFactory {

  @Composable
  override fun SettingsScreenComposable(
    currentRoute: String,
    goToNotes: () -> Unit,
    goToBlinkos: () -> Unit,
    goToTodoList: () -> Unit,
    goToSearch: () -> Unit,
    goToSettings: () -> Unit,
    exit: () -> Unit,
  ) {
    SettingsScreenComposableInternal(
      currentRoute = currentRoute,
      goToNotes = goToNotes,
      goToBlinkos = goToBlinkos,
      goToTodoList = goToTodoList,
      goToSearch = goToSearch,
      goToSettings = goToSettings,
      exit = exit,
    )
  }
}
