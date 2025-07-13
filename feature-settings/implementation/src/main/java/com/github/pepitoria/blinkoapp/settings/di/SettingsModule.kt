package com.github.pepitoria.blinkoapp.settings.di

import com.github.pepitoria.blinkoapp.settings.api.domain.GetDefaultTabUseCase
import com.github.pepitoria.blinkoapp.settings.api.domain.SetDefaultTabUseCase
import com.github.pepitoria.blinkoapp.settings.data.TabsRepository
import com.github.pepitoria.blinkoapp.settings.data.TabsRepositoryImpl
import com.github.pepitoria.blinkoapp.settings.domain.DefaultTabUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsModule {

  @Binds
  abstract fun bindTabsRepository(
    tabsRepositoryImpl: TabsRepositoryImpl
  ): TabsRepository

  @Binds
  abstract fun bindSetDefaultTabUseCase(
    defaultTabUseCaseImpl: DefaultTabUseCaseImpl
  ): SetDefaultTabUseCase

  @Binds
  abstract fun bindGetDefaultTabUseCase(
    defaultTabUseCaseImpl: DefaultTabUseCaseImpl
  ): GetDefaultTabUseCase
}