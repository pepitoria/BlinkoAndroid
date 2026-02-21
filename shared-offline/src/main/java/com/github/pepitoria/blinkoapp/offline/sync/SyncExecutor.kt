package com.github.pepitoria.blinkoapp.offline.sync

interface SyncExecutor {
  suspend fun executeCreate(
    localId: String,
    payload: SyncPayload,
  ): SyncResult
  suspend fun executeUpdate(
    localId: String,
    payload: SyncPayload,
  ): SyncResult
  suspend fun executeDelete(
    localId: String,
    serverId: Int?,
  ): SyncResult
}
