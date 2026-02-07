package com.github.pepitoria.blinkoapp.auth.data.net

import com.github.pepitoria.blinkoapp.auth.implementation.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object AuthRetrofit {

  @Provides
  @Singleton
  fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

  @Provides
  @Singleton
  fun provideAuthApiClient(
    impl: AuthApiClientNetImpl,
    localFakesApiClientImpl: AuthLocalFakesApiClientImpl,
  ): AuthApiClient {
    return if (BuildConfig.FLAVOR == "mockLocal") {
      localFakesApiClientImpl
    } else {
      impl
    }
  }
}
