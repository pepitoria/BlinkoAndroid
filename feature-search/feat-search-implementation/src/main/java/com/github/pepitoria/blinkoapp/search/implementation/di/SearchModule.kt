package com.github.pepitoria.blinkoapp.search.implementation.di

import com.github.pepitoria.blinkoapp.search.api.SearchFactory
import com.github.pepitoria.blinkoapp.search.implementation.SearchFactoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SearchModule {
    @Binds
    abstract fun bindSearchFactory(
        searchFactoryImpl: SearchFactoryImpl
    ): SearchFactory
}