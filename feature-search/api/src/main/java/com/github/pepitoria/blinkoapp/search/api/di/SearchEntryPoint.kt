package com.github.pepitoria.blinkoapp.search.api.di

import com.github.pepitoria.blinkoapp.search.api.SearchFactory
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SearchEntryPoint {
  fun searchFactory(): SearchFactory
}
