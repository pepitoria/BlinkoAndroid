package com.github.pepitoria.blinkoapp.offline.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "sync_queue",
  indices = [
    Index(value = ["noteLocalId"]),
    Index(value = ["createdAt"]),
  ],
)
data class SyncQueueEntity(
  @PrimaryKey(autoGenerate = true)
  val queueId: Long = 0,
  val noteLocalId: String,
  val noteServerId: Int? = null,
  val operation: Int,
  val payload: String,
  val createdAt: Long = System.currentTimeMillis(),
  val retryCount: Int = 0,
  val lastError: String? = null,
)
