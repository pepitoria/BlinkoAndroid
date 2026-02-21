package com.github.pepitoria.blinkoapp.offline.di

import android.content.Context
import androidx.room.Room
import com.github.pepitoria.blinkoapp.offline.connectivity.ConnectivityMonitor
import com.github.pepitoria.blinkoapp.offline.data.db.BlinkoDatabase
import com.github.pepitoria.blinkoapp.offline.data.db.dao.NoteDao
import com.github.pepitoria.blinkoapp.offline.data.db.dao.SyncQueueDao
import com.github.pepitoria.blinkoapp.offline.data.db.dao.TagDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OfflineModule {

  @Provides
  @Singleton
  fun provideDatabase(@ApplicationContext context: Context): BlinkoDatabase {
    return Room.databaseBuilder(
      context,
      BlinkoDatabase::class.java,
      BlinkoDatabase.DATABASE_NAME,
    ).build()
  }

  @Provides
  @Singleton
  fun provideNoteDao(database: BlinkoDatabase): NoteDao = database.noteDao()

  @Provides
  @Singleton
  fun provideSyncQueueDao(database: BlinkoDatabase): SyncQueueDao = database.syncQueueDao()

  @Provides
  @Singleton
  fun provideTagDao(database: BlinkoDatabase): TagDao = database.tagDao()

  @Provides
  @Singleton
  fun provideConnectivityMonitor(@ApplicationContext context: Context): ConnectivityMonitor {
    return ConnectivityMonitor(context)
  }
}
