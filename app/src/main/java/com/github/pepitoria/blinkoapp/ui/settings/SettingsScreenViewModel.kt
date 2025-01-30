package com.github.pepitoria.blinkoapp.ui.settings

import androidx.lifecycle.viewModelScope
import com.github.pepitoria.blinkoapp.domain.SessionUseCases
import com.github.pepitoria.blinkoapp.ui.base.BlinkoViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsScreenViewModel @Inject constructor(
  private val sessionUseCases: SessionUseCases,
): BlinkoViewModel() {

  private val _events = MutableSharedFlow<NavigationEvents>()
  val events = _events.asSharedFlow()

  fun logout() {
    sessionUseCases.logout()
    viewModelScope.launch {
      _events.emit(NavigationEvents.Exit)
    }
  }

  sealed class NavigationEvents {
    data object Exit : NavigationEvents()
  }
}
