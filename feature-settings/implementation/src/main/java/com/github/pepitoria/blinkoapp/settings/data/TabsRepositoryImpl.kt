package com.github.pepitoria.blinkoapp.settings.data

import com.github.pepitoria.blinkoapp.domain.LocalStorageUseCases
import com.github.pepitoria.blinkoapp.settings.api.domain.Tab
import javax.inject.Inject

class TabsRepositoryImpl @Inject constructor(
  private val localStorageUseCases: LocalStorageUseCases,
): TabsRepository {

  companion object {
    private const val DEFAULT_TAB_KEY = "com.github.pepitoria.blinkoapp.settings.data.default_tab"
  }
  override fun getDefaultTab(): Tab {
    return Tab.valueOf(localStorageUseCases.getString(DEFAULT_TAB_KEY)?: "BLINKOS")
  }

  override fun setDefaultTab(tab: Tab) {
    localStorageUseCases.saveString(DEFAULT_TAB_KEY, tab.name)
  }

}