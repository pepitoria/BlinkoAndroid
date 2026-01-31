package com.github.pepitoria.blinkoapp.settings.api

import androidx.compose.runtime.Composable

interface SettingsFactory {

  @Composable
  fun SettingsScreenComposable(
    currentRoute: String,
    goToNotes: () -> Unit,
    goToBlinkos: () -> Unit,
    goToTodoList: () -> Unit,
    goToSearch: () -> Unit,
    goToSettings: () -> Unit,
    exit: () -> Unit,
  )
}
