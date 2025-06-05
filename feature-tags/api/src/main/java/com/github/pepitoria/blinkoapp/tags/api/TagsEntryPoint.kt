package com.github.pepitoria.blinkoapp.tags.api

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@EntryPoint
@InstallIn(ActivityComponent::class)
interface TagsEntryPoint {
  fun getTagsFactory(): TagsFactory
}