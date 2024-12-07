package com.github.pepitoria.blinkoapp.data.repository

import com.github.pepitoria.blinkoapp.data.repository.auth.AuthenticationRepository
import com.github.pepitoria.blinkoapp.data.repository.auth.AuthenticationRepositoryApiImpl
import com.github.pepitoria.blinkoapp.data.repository.note.NoteRepository
import com.github.pepitoria.blinkoapp.data.repository.note.NoteRepositoryApiImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindAuthenticationRepository(
        authenticationRepositoryImpl: AuthenticationRepositoryApiImpl,
    ): AuthenticationRepository

    @Binds
    abstract fun bindNoteRepository(
        noteRepositoryImpl: NoteRepositoryApiImpl,
    ): NoteRepository
}