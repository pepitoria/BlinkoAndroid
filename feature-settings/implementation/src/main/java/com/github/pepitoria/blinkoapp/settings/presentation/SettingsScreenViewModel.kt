package com.github.pepitoria.blinkoapp.settings.presentation

import androidx.lifecycle.viewModelScope
import com.github.pepitoria.blinkoapp.auth.api.domain.SessionUseCases
import com.github.pepitoria.blinkoapp.settings.api.domain.GetDefaultTabUseCase
import com.github.pepitoria.blinkoapp.settings.api.domain.SetDefaultTabUseCase
import com.github.pepitoria.blinkoapp.settings.api.domain.Tab
import com.github.pepitoria.blinkoapp.shared.ui.base.BlinkoViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsScreenViewModel @Inject constructor(
  private val sessionUseCases: SessionUseCases,
  private val getDefaultTabUseCase: GetDefaultTabUseCase,
  private val setDefaultTabUseCase: SetDefaultTabUseCase,
) : BlinkoViewModel() {

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

  fun onTabToShowFirstSelected(tab: String) {
    setDefaultTabUseCase.setDefaultTab(Tab.valueOf(tab.uppercase()))
  }

  fun getDefaultTab(): String {
    return getDefaultTabUseCase.getDefaultTab().name.lowercase().replaceFirstChar { it.uppercase(Locale.getDefault()) }
  }
}
