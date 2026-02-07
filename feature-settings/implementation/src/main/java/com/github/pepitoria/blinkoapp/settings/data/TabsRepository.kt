package com.github.pepitoria.blinkoapp.settings.data

import com.github.pepitoria.blinkoapp.settings.api.domain.Tab

interface TabsRepository {

  fun getDefaultTab(): Tab
  fun setDefaultTab(tab: Tab)
}
