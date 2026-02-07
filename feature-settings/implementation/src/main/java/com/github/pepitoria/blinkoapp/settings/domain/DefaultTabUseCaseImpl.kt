package com.github.pepitoria.blinkoapp.settings.domain

import com.github.pepitoria.blinkoapp.settings.api.domain.GetDefaultTabUseCase
import com.github.pepitoria.blinkoapp.settings.api.domain.SetDefaultTabUseCase
import com.github.pepitoria.blinkoapp.settings.api.domain.Tab
import com.github.pepitoria.blinkoapp.settings.data.TabsRepository
import javax.inject.Inject

class DefaultTabUseCaseImpl @Inject constructor(
  private val tabsRepository: TabsRepository,
) : GetDefaultTabUseCase, SetDefaultTabUseCase {
  override fun getDefaultTab(): Tab {
    return tabsRepository.getDefaultTab()
  }

  override fun setDefaultTab(tab: Tab) {
    tabsRepository.setDefaultTab(tab)
  }
}
