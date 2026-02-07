package com.github.pepitoria.blinkoapp.notes.implementation.di

import com.github.pepitoria.blinkoapp.notes.api.NotesFactory
import com.github.pepitoria.blinkoapp.notes.api.domain.NoteRepository
import com.github.pepitoria.blinkoapp.notes.implementation.BuildConfig
import com.github.pepitoria.blinkoapp.notes.implementation.data.net.NotesApi
import com.github.pepitoria.blinkoapp.notes.implementation.data.net.NotesApiClient
import com.github.pepitoria.blinkoapp.notes.implementation.data.net.NotesApiClientNetImpl
import com.github.pepitoria.blinkoapp.notes.implementation.data.net.NotesLocalFakesApiClientImpl
import com.github.pepitoria.blinkoapp.notes.implementation.data.repository.NoteRepositoryApiImpl
import com.github.pepitoria.blinkoapp.notes.implementation.presentation.NotesFactoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotesModule {

  @Binds
  abstract fun bindNotesFactory(
    notesFactoryImpl: NotesFactoryImpl
  ): NotesFactory

  @Binds
  abstract fun bindNoteRepository(
    noteRepositoryImpl: NoteRepositoryApiImpl
  ): NoteRepository

  companion object {
    @Provides
    @Singleton
    fun provideNotesApi(retrofit: Retrofit): NotesApi =
      retrofit.create(NotesApi::class.java)

    @Provides
    @Singleton
    fun provideNotesApiClient(
      impl: NotesApiClientNetImpl,
      localFakesApiClientImpl: NotesLocalFakesApiClientImpl,
    ): NotesApiClient {
      if (BuildConfig.FLAVOR == "mockLocal") {
        return localFakesApiClientImpl
      } else {
        return impl
      }
    }
  }
}
