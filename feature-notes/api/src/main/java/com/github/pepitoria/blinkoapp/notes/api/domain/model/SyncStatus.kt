package com.github.pepitoria.blinkoapp.notes.api.domain.model

enum class SyncStatus {
  SYNCED,
  PENDING_CREATE,
  PENDING_UPDATE,
  PENDING_DELETE,
  CONFLICT,
}
