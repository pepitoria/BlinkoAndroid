package com.github.pepitoria.blinkoapp.notes.api

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface NotesEntryPoint {
  fun notesFactory(): NotesFactory
}
