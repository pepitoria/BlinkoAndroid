package com.github.pepitoria.blinkoapp.settings.api.domain

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@EntryPoint
@InstallIn(ActivityComponent::class)
interface SettingsEntryPoint {
  fun getDefaultTabUseCase(): GetDefaultTabUseCase
}