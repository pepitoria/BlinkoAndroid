package com.github.pepitoria.blinkoapp.ui.settings

import androidx.lifecycle.viewModelScope
import com.github.pepitoria.blinkoapp.domain.SessionUseCases
import com.github.pepitoria.blinkoapp.ui.base.BlinkoViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsScreenViewModel @Inject constructor(
  private val sessionUseCases: SessionUseCases,
): BlinkoViewModel() {

  sealed class Events {
    data object Exit : Events()
  }

  private val _events = MutableSharedFlow<Events>()
  val events = _events.asSharedFlow()

  fun logout() {
    sessionUseCases.logout()
    viewModelScope.launch {
      _events.emit(Events.Exit)
    }
  }
}
