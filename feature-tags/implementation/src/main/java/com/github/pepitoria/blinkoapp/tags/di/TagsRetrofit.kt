package com.github.pepitoria.blinkoapp.tags.di

import com.github.pepitoria.blinkoapp.tags.data.net.TagsApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object TagsRetrofit {

  @Singleton
  @Provides
  fun provideTagsApi(retrofit: Retrofit): TagsApi {
    return retrofit.create(TagsApi::class.java)
  }
}
