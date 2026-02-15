package com.github.pepitoria.blinkoapp.notes.implementation.presentation

import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNote
import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNoteType
import com.github.pepitoria.blinkoapp.notes.implementation.domain.NoteDeleteUseCase
import com.github.pepitoria.blinkoapp.notes.implementation.domain.NoteListUseCase
import com.github.pepitoria.blinkoapp.notes.implementation.domain.NoteUpsertUseCase
import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NoteListScreenViewModelTest {

  private lateinit var viewModel: NoteListScreenViewModel

  private val noteListUseCase: NoteListUseCase = mockk(relaxed = true)
  private val noteDeleteUseCase: NoteDeleteUseCase = mockk(relaxed = true)
  private val noteUpsertUseCase: NoteUpsertUseCase = mockk(relaxed = true)

  private val testDispatcher = StandardTestDispatcher()
  private val testScope = TestScope(testDispatcher)

  @BeforeEach
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    viewModel = NoteListScreenViewModel(
      noteListUseCase = noteListUseCase,
      noteDeleteUseCase = noteDeleteUseCase,
      noteUpsertUseCase = noteUpsertUseCase,
    )
  }

  @AfterEach
  fun tearDown() {
    Dispatchers.resetMain()
  }

  // setNoteType tests (synchronous)

  @Test
  fun `setNoteType updates noteType state`() = testScope.runTest {
    assertEquals(BlinkoNoteType.BLINKO, viewModel.noteType.value)

    viewModel.setNoteType(BlinkoNoteType.NOTE)

    assertEquals(BlinkoNoteType.NOTE, viewModel.noteType.value)
  }

  @Test
  fun `setNoteType to TODO fetches archived notes`() = testScope.runTest {
    coEvery {
      noteListUseCase.listNotes(type = BlinkoNoteType.TODO.value, archived = true)
    } returns BlinkoResult.Success(emptyList())

    viewModel.setNoteType(BlinkoNoteType.TODO)
    advanceUntilIdle()

    coVerify(atLeast = 1) {
      noteListUseCase.listNotes(type = BlinkoNoteType.TODO.value, archived = true)
    }
  }

  // onStart tests

  @Test
  fun `onStart calls noteListUseCase with current noteType`() = testScope.runTest {
    coEvery {
      noteListUseCase.listNotes(type = BlinkoNoteType.BLINKO.value)
    } returns BlinkoResult.Success(emptyList())

    viewModel.onStart()
    advanceUntilIdle()

    coVerify(atLeast = 1) {
      noteListUseCase.listNotes(type = BlinkoNoteType.BLINKO.value)
    }
  }

  @Test
  fun `refresh calls onStart`() = testScope.runTest {
    coEvery {
      noteListUseCase.listNotes(type = any(), archived = any())
    } returns BlinkoResult.Success(emptyList())

    viewModel.refresh()
    advanceUntilIdle()

    coVerify(atLeast = 1) {
      noteListUseCase.listNotes(type = BlinkoNoteType.BLINKO.value)
    }
  }

  // deleteNote tests

  @Test
  fun `deleteNote calls noteDeleteUseCase with note id`() = testScope.runTest {
    val note = BlinkoNote(
      id = 42,
      content = "Test note",
      type = BlinkoNoteType.BLINKO,
      isArchived = false,
    )
    coEvery { noteDeleteUseCase.deleteNote(42) } returns BlinkoResult.Success(true)

    viewModel.deleteNote(note)
    advanceUntilIdle()

    coVerify(exactly = 1) { noteDeleteUseCase.deleteNote(42) }
  }

  @Test
  fun `deleteNote does nothing when note id is null`() = testScope.runTest {
    val note = BlinkoNote(
      id = null,
      content = "Test note",
      type = BlinkoNoteType.BLINKO,
      isArchived = false,
    )

    viewModel.deleteNote(note)
    advanceUntilIdle()

    coVerify(exactly = 0) { noteDeleteUseCase.deleteNote(any()) }
  }

  // markNoteAsDone tests

  @Test
  fun `markNoteAsDone calls noteUpsertUseCase with note`() = testScope.runTest {
    val note = BlinkoNote(
      id = 1,
      content = "Test note",
      type = BlinkoNoteType.TODO,
      isArchived = true,
    )
    coEvery { noteUpsertUseCase.upsertNote(note) } returns BlinkoResult.Success(note)

    viewModel.markNoteAsDone(note)
    advanceUntilIdle()

    coVerify(exactly = 1) { noteUpsertUseCase.upsertNote(note) }
  }
}
