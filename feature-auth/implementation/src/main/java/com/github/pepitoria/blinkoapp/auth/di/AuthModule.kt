package com.github.pepitoria.blinkoapp.auth.di

import com.github.pepitoria.blinkoapp.auth.api.AuthFactory
import com.github.pepitoria.blinkoapp.auth.api.domain.SessionUseCases
import com.github.pepitoria.blinkoapp.auth.data.repository.AuthenticationRepositoryImpl
import com.github.pepitoria.blinkoapp.auth.domain.SessionUseCasesImpl
import com.github.pepitoria.blinkoapp.auth.presentation.AuthFactoryImpl
import com.github.pepitoria.blinkoapp.domain.data.AuthenticationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

  @Binds
  abstract fun bindAuthFactory(
    authFactoryImpl: AuthFactoryImpl
  ): AuthFactory

  @Binds
  abstract fun bindSessionUseCases(
    sessionUseCasesImpl: SessionUseCasesImpl
  ): SessionUseCases

  @Binds
  abstract fun bindAuthenticationRepository(
    authenticationRepositoryImpl: AuthenticationRepositoryImpl
  ): AuthenticationRepository
}
