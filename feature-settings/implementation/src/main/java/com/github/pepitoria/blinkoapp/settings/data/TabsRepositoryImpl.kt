package com.github.pepitoria.blinkoapp.settings.data

import com.github.pepitoria.blinkoapp.settings.api.domain.Tab
import com.github.pepitoria.blinkoapp.shared.domain.data.LocalStorage
import javax.inject.Inject

class TabsRepositoryImpl @Inject constructor(
  private val localStorage: LocalStorage,
) : TabsRepository {

  companion object {
    private const val DEFAULT_TAB_KEY = "com.github.pepitoria.blinkoapp.settings.data.default_tab"
  }
  override fun getDefaultTab(): Tab {
    return Tab.valueOf(localStorage.getString(DEFAULT_TAB_KEY) ?: "BLINKOS")
  }

  override fun setDefaultTab(tab: Tab) {
    localStorage.saveString(DEFAULT_TAB_KEY, tab.name)
  }
}
