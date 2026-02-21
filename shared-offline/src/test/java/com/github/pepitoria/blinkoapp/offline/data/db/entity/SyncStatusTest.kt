package com.github.pepitoria.blinkoapp.offline.data.db.entity

import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

class SyncStatusTest {

  @Test
  fun `fromValue returns correct status for valid values`() {
    assertEquals(SyncStatus.SYNCED, SyncStatus.fromValue(0))
    assertEquals(SyncStatus.PENDING_CREATE, SyncStatus.fromValue(1))
    assertEquals(SyncStatus.PENDING_UPDATE, SyncStatus.fromValue(2))
    assertEquals(SyncStatus.PENDING_DELETE, SyncStatus.fromValue(3))
    assertEquals(SyncStatus.CONFLICT, SyncStatus.fromValue(4))
  }

  @Test
  fun `fromValue returns SYNCED for invalid values`() {
    assertEquals(SyncStatus.SYNCED, SyncStatus.fromValue(-1))
    assertEquals(SyncStatus.SYNCED, SyncStatus.fromValue(100))
  }

  @Test
  fun `value returns correct int for each status`() {
    assertEquals(0, SyncStatus.SYNCED.value)
    assertEquals(1, SyncStatus.PENDING_CREATE.value)
    assertEquals(2, SyncStatus.PENDING_UPDATE.value)
    assertEquals(3, SyncStatus.PENDING_DELETE.value)
    assertEquals(4, SyncStatus.CONFLICT.value)
  }
}
