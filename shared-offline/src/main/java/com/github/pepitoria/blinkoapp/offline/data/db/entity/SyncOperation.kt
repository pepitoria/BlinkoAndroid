package com.github.pepitoria.blinkoapp.offline.data.db.entity

enum class SyncOperation(val value: Int) {
  CREATE(0),
  UPDATE(1),
  DELETE(2),
  ;

  companion object {
    fun fromValue(value: Int): SyncOperation = entries.firstOrNull { it.value == value } ?: CREATE
  }
}
