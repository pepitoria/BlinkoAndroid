package com.github.pepitoria.blinkoapp.data.net

import com.github.pepitoria.blinkoapp.data.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {
 const val TIMEOUT = 30L

  @Provides
  @Singleton
  fun provideBlinkoApi(retrofit: Retrofit): BlinkoApi =
    retrofit.create(BlinkoApi::class.java)

  @Singleton
  @Provides
  fun getRetrofit(okHttpClient: OkHttpClient): Retrofit {
    return Retrofit.Builder()
      .client(okHttpClient)
      .baseUrl("https://blinko-demo.vercel.app")
      .addConverterFactory(GsonConverterFactory.create())
      .build()
  }

  @Provides
  @Singleton
  fun getHttpClient(): OkHttpClient {
    val okHttpClient = OkHttpClient()
      .newBuilder()
      .connectTimeout(TIMEOUT, SECONDS)
      .readTimeout(TIMEOUT, SECONDS)
      .writeTimeout(TIMEOUT, SECONDS)
      .addInterceptor(
        HttpLoggingInterceptor().setLevel(
          if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE,
        ),
      )

    return okHttpClient.build()
  }
}