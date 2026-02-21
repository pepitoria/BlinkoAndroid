package com.github.pepitoria.blinkoapp.offline.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
  tableName = "notes",
  indices = [
    Index(value = ["serverId"], unique = true),
    Index(value = ["syncStatus"]),
    Index(value = ["type", "isArchived"]),
  ],
)
data class NoteEntity(
  @PrimaryKey
  val localId: String = UUID.randomUUID().toString(),
  val serverId: Int? = null,
  val content: String,
  val type: Int,
  val isArchived: Boolean,
  val createdAt: String? = null,
  val updatedAt: String? = null,
  val serverUpdatedAt: String? = null,
  val localUpdatedAt: Long = System.currentTimeMillis(),
  val syncStatus: Int = SyncStatus.SYNCED.value,
)
