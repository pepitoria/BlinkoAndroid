package com.github.pepitoria.blinkoapp.offline.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.github.pepitoria.blinkoapp.offline.data.db.dao.NoteDao
import com.github.pepitoria.blinkoapp.offline.data.db.dao.SyncQueueDao
import com.github.pepitoria.blinkoapp.offline.data.db.dao.TagDao
import com.github.pepitoria.blinkoapp.offline.data.db.entity.NoteEntity
import com.github.pepitoria.blinkoapp.offline.data.db.entity.SyncQueueEntity
import com.github.pepitoria.blinkoapp.offline.data.db.entity.TagEntity

@Database(
  entities = [
    NoteEntity::class,
    SyncQueueEntity::class,
    TagEntity::class,
  ],
  version = 1,
  exportSchema = false,
)
abstract class BlinkoDatabase : RoomDatabase() {
  abstract fun noteDao(): NoteDao
  abstract fun syncQueueDao(): SyncQueueDao
  abstract fun tagDao(): TagDao

  companion object {
    const val DATABASE_NAME = "blinko_database"
  }
}
