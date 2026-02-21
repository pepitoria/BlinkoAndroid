package com.github.pepitoria.blinkoapp.offline.data.db.entity

enum class SyncStatus(val value: Int) {
  SYNCED(0),
  PENDING_CREATE(1),
  PENDING_UPDATE(2),
  PENDING_DELETE(3),
  CONFLICT(4),
  ;

  companion object {
    fun fromValue(value: Int): SyncStatus = entries.firstOrNull { it.value == value } ?: SYNCED
  }
}
