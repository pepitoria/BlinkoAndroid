package com.github.pepitoria.blinkoapp.notes.implementation.data.repository

import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNote
import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNoteType
import com.github.pepitoria.blinkoapp.notes.api.domain.model.SyncStatus
import com.github.pepitoria.blinkoapp.notes.implementation.data.model.notelist.NoteResponse
import com.github.pepitoria.blinkoapp.notes.implementation.data.net.NotesApiClient
import com.github.pepitoria.blinkoapp.offline.connectivity.ConnectivityMonitor
import com.github.pepitoria.blinkoapp.offline.data.db.dao.NoteDao
import com.github.pepitoria.blinkoapp.offline.data.db.entity.NoteEntity
import com.github.pepitoria.blinkoapp.offline.data.db.entity.SyncStatus as EntitySyncStatus
import com.github.pepitoria.blinkoapp.offline.sync.SyncQueueManager
import com.github.pepitoria.blinkoapp.shared.domain.data.AuthenticationRepository
import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoResult
import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoSession
import com.github.pepitoria.blinkoapp.shared.networking.model.ApiResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OfflineFirstNoteRepositoryTest {

  private lateinit var api: NotesApiClient
  private lateinit var authRepository: AuthenticationRepository
  private lateinit var noteDao: NoteDao
  private lateinit var syncQueueManager: SyncQueueManager
  private lateinit var connectivityMonitor: ConnectivityMonitor
  private lateinit var repository: OfflineFirstNoteRepository

  private val isConnectedFlow = MutableStateFlow(true)

  private val testDispatcher = StandardTestDispatcher()
  private val testScope = TestScope(testDispatcher)

  @BeforeEach
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    api = mockk(relaxed = true)
    authRepository = mockk()
    noteDao = mockk(relaxed = true)
    syncQueueManager = mockk(relaxed = true)
    connectivityMonitor = mockk {
      every { isConnected } returns isConnectedFlow
    }

    coEvery { authRepository.getSession() } returns BlinkoSession(
      url = "https://test.com",
      token = "test-token",
      userName = "test-user",
      password = "test-password",
    )

    repository = OfflineFirstNoteRepository(
      api = api,
      authenticationRepository = authRepository,
      noteDao = noteDao,
      syncQueueManager = syncQueueManager,
      connectivityMonitor = connectivityMonitor,
    )
  }

  @AfterEach
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `list returns local notes when offline`() = testScope.runTest {
    isConnectedFlow.value = false
    val localNotes = listOf(
      createNoteEntity("local-1", serverId = 1, content = "Note 1"),
      createNoteEntity("local-2", serverId = 2, content = "Note 2"),
    )
    coEvery { noteDao.list(type = 0, archived = false) } returns localNotes

    val result = repository.list(
      url = "https://test.com",
      token = "token",
      type = 0,
      archived = false,
    )

    assertIs<BlinkoResult.Success<List<BlinkoNote>>>(result)
    assertEquals(2, result.value.size)
    assertEquals("Note 1", result.value[0].content)
  }

  @Test
  fun `list fetches from server when online and merges`() = testScope.runTest {
    isConnectedFlow.value = true
    val serverNotes = listOf(
      NoteResponse(id = 1, content = "Server Note 1", type = 0, isArchived = false),
    )
    coEvery { api.noteList(any(), any(), any()) } returns ApiResult.ApiSuccess(serverNotes)
    coEvery { noteDao.list(type = 0, archived = false) } returns listOf(
      createNoteEntity("local-1", serverId = 1, content = "Server Note 1"),
    )

    val result = repository.list(
      url = "https://test.com",
      token = "token",
      type = 0,
      archived = false,
    )

    assertIs<BlinkoResult.Success<List<BlinkoNote>>>(result)
    coVerify { api.noteList(any(), any(), any()) }
  }

  @Test
  fun `upsertNote creates local note and queues sync when offline`() = testScope.runTest {
    isConnectedFlow.value = false
    val newNote = BlinkoNote(
      id = null,
      localId = null,
      content = "New offline note",
      type = BlinkoNoteType.BLINKO,
      isArchived = false,
    )

    val result = repository.upsertNote(newNote)

    assertIs<BlinkoResult.Success<BlinkoNote>>(result)
    assertEquals("New offline note", result.value.content)
    assertEquals(SyncStatus.PENDING_CREATE, result.value.syncStatus)

    coVerify { noteDao.insert(any()) }
    coVerify { syncQueueManager.enqueueCreate(any()) }
  }

  @Test
  fun `upsertNote syncs immediately when online`() = testScope.runTest {
    isConnectedFlow.value = true
    val newNote = BlinkoNote(
      id = null,
      localId = null,
      content = "New note",
      type = BlinkoNoteType.BLINKO,
      isArchived = false,
    )
    val serverResponse = NoteResponse(
      id = 100,
      content = "New note",
      type = 0,
      isArchived = false,
      updatedAt = "2024-01-01T00:00:00Z",
    )
    coEvery { api.upsertNote(any(), any(), any()) } returns ApiResult.ApiSuccess(serverResponse)

    val result = repository.upsertNote(newNote)

    assertIs<BlinkoResult.Success<BlinkoNote>>(result)
    coVerify { api.upsertNote(any(), any(), any()) }
  }

  @Test
  fun `deleteNote queues delete when offline`() = testScope.runTest {
    isConnectedFlow.value = false
    val noteEntity = createNoteEntity("local-1", serverId = 100)
    coEvery { noteDao.getByLocalId("local-1") } returns noteEntity

    val noteToDelete = BlinkoNote(
      id = 100,
      localId = "local-1",
      content = "To delete",
      type = BlinkoNoteType.BLINKO,
      isArchived = false,
    )

    val result = repository.deleteNote(noteToDelete)

    assertIs<BlinkoResult.Success<Boolean>>(result)
    coVerify { syncQueueManager.enqueueDelete(any()) }
  }

  @Test
  fun `deleteNote syncs immediately when online`() = testScope.runTest {
    isConnectedFlow.value = true
    val noteEntity = createNoteEntity("local-1", serverId = 100)
    coEvery { noteDao.getByLocalId("local-1") } returns noteEntity
    coEvery { api.deleteNote(any(), any(), any()) } returns ApiResult.ApiSuccess(mockk())

    val noteToDelete = BlinkoNote(
      id = 100,
      localId = "local-1",
      content = "To delete",
      type = BlinkoNoteType.BLINKO,
      isArchived = false,
    )

    val result = repository.deleteNote(noteToDelete)

    assertIs<BlinkoResult.Success<Boolean>>(result)
    coVerify { api.deleteNote(any(), any(), any()) }
    coVerify { noteDao.deleteByLocalId("local-1") }
  }

  @Test
  fun `resolveConflict with keepLocal re-queues update`() = testScope.runTest {
    val noteEntity = createNoteEntity(
      localId = "local-1",
      serverId = 100,
      syncStatus = EntitySyncStatus.CONFLICT.value,
    )
    coEvery { noteDao.getByLocalId("local-1") } returns noteEntity

    val conflictNote = BlinkoNote(
      id = 100,
      localId = "local-1",
      content = "Conflicted",
      type = BlinkoNoteType.BLINKO,
      isArchived = false,
      syncStatus = SyncStatus.CONFLICT,
    )

    isConnectedFlow.value = false
    val result = repository.resolveConflict(conflictNote, keepLocal = true)

    assertIs<BlinkoResult.Success<BlinkoNote>>(result)
    coVerify { noteDao.updateSyncStatus("local-1", EntitySyncStatus.PENDING_UPDATE.value) }
    coVerify { syncQueueManager.enqueueUpdate(any()) }
  }

  private fun createNoteEntity(
    localId: String,
    serverId: Int? = null,
    content: String = "Test content",
    syncStatus: Int = EntitySyncStatus.SYNCED.value,
  ): NoteEntity {
    return NoteEntity(
      localId = localId,
      serverId = serverId,
      content = content,
      type = 0,
      isArchived = false,
      syncStatus = syncStatus,
    )
  }
}
