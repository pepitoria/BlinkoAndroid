package com.github.pepitoria.blinkoapp.tags.di

import com.github.pepitoria.blinkoapp.tags.api.TagsFactory
import com.github.pepitoria.blinkoapp.tags.data.TagsRepository
import com.github.pepitoria.blinkoapp.tags.data.TagsRepositoryImpl
import com.github.pepitoria.blinkoapp.tags.data.net.TagsApiClient
import com.github.pepitoria.blinkoapp.tags.data.net.TagsApiClientNetImpl
import com.github.pepitoria.blinkoapp.tags.presentation.TagsFactoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class TagsModule {
  @Binds
  abstract fun bindTagsFactory(
    tagsFactoryImpl: TagsFactoryImpl
  ): TagsFactory

  @Binds
  abstract fun bindTagsRepository(
    tagsRepositoryImpl: TagsRepositoryImpl
  ): TagsRepository

  @Binds
  abstract fun bindTagsApiClient(
    tagsApiClient: TagsApiClientNetImpl
  ): TagsApiClient

}