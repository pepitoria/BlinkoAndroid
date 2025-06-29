package com.github.pepitoria.blinkoapp.ui.settings

import androidx.lifecycle.viewModelScope
import com.github.pepitoria.blinkoapp.domain.LocalStorageUseCases
import com.github.pepitoria.blinkoapp.domain.SessionUseCases
import com.github.pepitoria.blinkoapp.ui.base.BlinkoViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettingsScreenViewModel @Inject constructor(
  private val sessionUseCases: SessionUseCases,
  private val localStorageUseCases: LocalStorageUseCases,
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

  fun saveTabsOrder(tabs: List<String>) {
    Timber.w("Saving tabs order: $tabs")
    localStorageUseCases.saveStringSet(
      key = "tabs_order",
      values = tabs
    )
  }

  fun getTabsOrder(): Set<String>? {
    val tabsOrder = localStorageUseCases.getStringSet("tabs_order")
    Timber.w("getting tabs order: $tabsOrder")

    return tabsOrder
  }
}
