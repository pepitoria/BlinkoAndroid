package com.github.pepitoria.blinkoapp.auth.api

import com.github.pepitoria.blinkoapp.auth.api.domain.SessionUseCases
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AuthEntryPoint {
  fun sessionUseCases(): SessionUseCases
}
