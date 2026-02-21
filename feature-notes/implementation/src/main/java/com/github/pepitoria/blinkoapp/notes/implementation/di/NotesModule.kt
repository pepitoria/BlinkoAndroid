package com.github.pepitoria.blinkoapp.notes.implementation.di

import com.github.pepitoria.blinkoapp.notes.api.NotesFactory
import com.github.pepitoria.blinkoapp.notes.api.domain.NoteRepository
import com.github.pepitoria.blinkoapp.notes.implementation.BuildConfig
import com.github.pepitoria.blinkoapp.notes.implementation.data.net.NotesApi
import com.github.pepitoria.blinkoapp.notes.implementation.data.net.NotesApiClient
import com.github.pepitoria.blinkoapp.notes.implementation.data.net.NotesApiClientNetImpl
import com.github.pepitoria.blinkoapp.notes.implementation.data.net.NotesLocalFakesApiClientImpl
import com.github.pepitoria.blinkoapp.notes.implementation.data.repository.OfflineFirstNoteRepository
import com.github.pepitoria.blinkoapp.notes.implementation.data.sync.NoteSyncExecutor
import com.github.pepitoria.blinkoapp.notes.implementation.presentation.NotesFactoryImpl
import com.github.pepitoria.blinkoapp.offline.sync.SyncExecutor
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class NotesModule {

  @Binds
  abstract fun bindNotesFactory(notesFactoryImpl: NotesFactoryImpl): NotesFactory

  @Binds
  abstract fun bindNoteRepository(noteRepositoryImpl: OfflineFirstNoteRepository): NoteRepository

  @Binds
  abstract fun bindSyncExecutor(noteSyncExecutor: NoteSyncExecutor): SyncExecutor

  companion object {
    @Provides
    @Singleton
    fun provideNotesApi(retrofit: Retrofit): NotesApi = retrofit.create(NotesApi::class.java)

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
