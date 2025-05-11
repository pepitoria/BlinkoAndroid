package com.github.pepitoria.blinkoapp.data.net

import com.github.pepitoria.blinkoapp.data.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class BlinkoApiModule {

  @Provides
  @Singleton
  fun provideBlinkoApi(
    impl: BlinkoApiClientNetImpl,
    localFakesApiClientImpl: BlinkoLocalFakesApiClientImpl,
  ): BlinkoApiClient {
    if (BuildConfig.FLAVOR == "mockLocal") {
      return localFakesApiClientImpl
    } else {
      return impl
    }

  }
}