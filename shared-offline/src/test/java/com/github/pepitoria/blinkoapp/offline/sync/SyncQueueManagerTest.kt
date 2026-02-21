package com.github.pepitoria.blinkoapp.offline.sync

import com.github.pepitoria.blinkoapp.offline.data.db.dao.NoteDao
import com.github.pepitoria.blinkoapp.offline.data.db.dao.SyncQueueDao
import com.github.pepitoria.blinkoapp.offline.data.db.entity.NoteEntity
import com.github.pepitoria.blinkoapp.offline.data.db.entity.SyncOperation
import com.github.pepitoria.blinkoapp.offline.data.db.entity.SyncQueueEntity
import com.github.pepitoria.blinkoapp.offline.data.db.entity.SyncStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SyncQueueManagerTest {

  private lateinit var syncQueueDao: SyncQueueDao
  private lateinit var noteDao: NoteDao
  private lateinit var syncQueueManager: SyncQueueManager

  @BeforeEach
  fun setup() {
    syncQueueDao = mockk(relaxed = true)
    noteDao = mockk(relaxed = true)
    syncQueueManager = SyncQueueManager(syncQueueDao, noteDao)
  }

  @Test
  fun `enqueueCreate should insert CREATE operation`() = runTest {
    val note = createTestNote(localId = "local-1", serverId = null)

    syncQueueManager.enqueueCreate(note)

    val slot = slot<SyncQueueEntity>()
    coVerify { syncQueueDao.insert(capture(slot)) }

    assertEquals(SyncOperation.CREATE.value, slot.captured.operation)
    assertEquals("local-1", slot.captured.noteLocalId)
    coVerify { noteDao.updateSyncStatus("local-1", SyncStatus.PENDING_CREATE.value) }
  }

  @Test
  fun `enqueueUpdate with no existing entry should add UPDATE`() = runTest {
    val note = createTestNote(localId = "local-1", serverId = 100)
    coEvery { syncQueueDao.getLatestForNote("local-1") } returns null

    syncQueueManager.enqueueUpdate(note)

    val slot = slot<SyncQueueEntity>()
    coVerify { syncQueueDao.insert(capture(slot)) }
    assertEquals(SyncOperation.UPDATE.value, slot.captured.operation)
  }

  @Test
  fun `enqueueUpdate after CREATE should merge into CREATE`() = runTest {
    val note = createTestNote(localId = "local-1", serverId = null, content = "Updated content")
    val existingCreate = SyncQueueEntity(
      queueId = 1L,
      noteLocalId = "local-1",
      noteServerId = null,
      operation = SyncOperation.CREATE.value,
      payload = SyncPayload(content = "Original", type = 0, isArchived = false).toJson(),
    )
    coEvery { syncQueueDao.getLatestForNote("local-1") } returns existingCreate

    syncQueueManager.enqueueUpdate(note)

    val slot = slot<SyncQueueEntity>()
    coVerify { syncQueueDao.update(capture(slot)) }
    assertEquals(SyncOperation.CREATE.value, slot.captured.operation)
    val payload = SyncPayload.fromJson(slot.captured.payload)
    assertEquals("Updated content", payload.content)
  }

  @Test
  fun `enqueueUpdate after UPDATE should replace payload`() = runTest {
    val note = createTestNote(localId = "local-1", serverId = 100, content = "Updated again")
    val existingUpdate = SyncQueueEntity(
      queueId = 1L,
      noteLocalId = "local-1",
      noteServerId = 100,
      operation = SyncOperation.UPDATE.value,
      payload = SyncPayload(serverId = 100, content = "First update", type = 0, isArchived = false).toJson(),
    )
    coEvery { syncQueueDao.getLatestForNote("local-1") } returns existingUpdate

    syncQueueManager.enqueueUpdate(note)

    val slot = slot<SyncQueueEntity>()
    coVerify { syncQueueDao.update(capture(slot)) }
    val payload = SyncPayload.fromJson(slot.captured.payload)
    assertEquals("Updated again", payload.content)
  }

  @Test
  fun `enqueueDelete with no existing entry and serverId should add DELETE`() = runTest {
    val note = createTestNote(localId = "local-1", serverId = 100)
    coEvery { syncQueueDao.getLatestForNote("local-1") } returns null

    syncQueueManager.enqueueDelete(note)

    val slot = slot<SyncQueueEntity>()
    coVerify { syncQueueDao.insert(capture(slot)) }
    assertEquals(SyncOperation.DELETE.value, slot.captured.operation)
    coVerify { noteDao.updateSyncStatus("local-1", SyncStatus.PENDING_DELETE.value) }
  }

  @Test
  fun `enqueueDelete for local-only note should delete immediately`() = runTest {
    val note = createTestNote(localId = "local-1", serverId = null)
    coEvery { syncQueueDao.getLatestForNote("local-1") } returns null

    syncQueueManager.enqueueDelete(note)

    coVerify { noteDao.deleteByLocalId("local-1") }
    coVerify(exactly = 0) { syncQueueDao.insert(any()) }
  }

  @Test
  fun `enqueueDelete after CREATE should remove from queue and delete locally`() = runTest {
    val note = createTestNote(localId = "local-1", serverId = null)
    val existingCreate = SyncQueueEntity(
      queueId = 1L,
      noteLocalId = "local-1",
      noteServerId = null,
      operation = SyncOperation.CREATE.value,
      payload = SyncPayload(content = "New note", type = 0, isArchived = false).toJson(),
    )
    coEvery { syncQueueDao.getLatestForNote("local-1") } returns existingCreate

    syncQueueManager.enqueueDelete(note)

    coVerify { syncQueueDao.deleteByNoteLocalId("local-1") }
    coVerify { noteDao.deleteByLocalId("local-1") }
    coVerify(exactly = 0) { syncQueueDao.insert(any()) }
  }

  @Test
  fun `enqueueDelete after UPDATE should clear queue and add DELETE`() = runTest {
    val note = createTestNote(localId = "local-1", serverId = 100)
    val existingUpdate = SyncQueueEntity(
      queueId = 1L,
      noteLocalId = "local-1",
      noteServerId = 100,
      operation = SyncOperation.UPDATE.value,
      payload = SyncPayload(serverId = 100, content = "Updated", type = 0, isArchived = false).toJson(),
    )
    coEvery { syncQueueDao.getLatestForNote("local-1") } returns existingUpdate

    syncQueueManager.enqueueDelete(note)

    coVerify { syncQueueDao.deleteByNoteLocalId("local-1") }
    val slot = slot<SyncQueueEntity>()
    coVerify { syncQueueDao.insert(capture(slot)) }
    assertEquals(SyncOperation.DELETE.value, slot.captured.operation)
  }

  private fun createTestNote(
    localId: String,
    serverId: Int?,
    content: String = "Test content",
  ): NoteEntity {
    return NoteEntity(
      localId = localId,
      serverId = serverId,
      content = content,
      type = 0,
      isArchived = false,
    )
  }
}
