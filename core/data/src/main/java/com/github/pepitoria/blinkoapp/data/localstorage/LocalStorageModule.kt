package com.github.pepitoria.blinkoapp.data.localstorage

import com.github.pepitoria.blinkoapp.domain.data.LocalStorage
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocalStorageModule {

    @Singleton
    @Binds
    abstract fun bindLocalStorage(
        localStorageImplementation: LocalStorageSharedPreferences,
    ): LocalStorage
}
