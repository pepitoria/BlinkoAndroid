package com.github.pepitoria.blinkoapp.tags.di

import com.github.pepitoria.blinkoapp.tags.TagsFactoryImpl
import com.github.pepitoria.blinkoapp.tags.api.TagsFactory
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
}