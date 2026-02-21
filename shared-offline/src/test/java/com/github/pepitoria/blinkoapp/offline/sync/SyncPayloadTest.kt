package com.github.pepitoria.blinkoapp.offline.sync

import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.jupiter.api.Test

class SyncPayloadTest {

  @Test
  fun `toJson and fromJson roundtrip preserves data`() {
    val original = SyncPayload(
      serverId = 123,
      content = "Test content",
      type = 1,
      isArchived = true,
    )

    val json = original.toJson()
    val restored = SyncPayload.fromJson(json)

    assertEquals(original.serverId, restored.serverId)
    assertEquals(original.content, restored.content)
    assertEquals(original.type, restored.type)
    assertEquals(original.isArchived, restored.isArchived)
  }

  @Test
  fun `toJson and fromJson handles null serverId`() {
    val original = SyncPayload(
      serverId = null,
      content = "New note",
      type = 0,
      isArchived = false,
    )

    val json = original.toJson()
    val restored = SyncPayload.fromJson(json)

    assertNull(restored.serverId)
    assertEquals(original.content, restored.content)
  }

  @Test
  fun `toJson handles special characters in content`() {
    val original = SyncPayload(
      content = "Line 1\nLine 2\tTab \"Quotes\" 'Single'",
      type = 0,
      isArchived = false,
    )

    val json = original.toJson()
    val restored = SyncPayload.fromJson(json)

    assertEquals(original.content, restored.content)
  }

  @Test
  fun `toJson handles unicode content`() {
    val original = SyncPayload(
      content = "Hello World! \uD83D\uDE00 \uD83C\uDFE0",
      type = 0,
      isArchived = false,
    )

    val json = original.toJson()
    val restored = SyncPayload.fromJson(json)

    assertEquals(original.content, restored.content)
  }
}
