package com.github.pepitoria.blinkoapp.notes.implementation.presentation

import androidx.lifecycle.viewModelScope
import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNote
import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNoteType
import com.github.pepitoria.blinkoapp.notes.implementation.domain.NoteListByIdsUseCase
import com.github.pepitoria.blinkoapp.notes.implementation.domain.NoteUpsertUseCase
import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
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
class NoteEditScreenViewModelTest {

  private lateinit var viewModel: NoteEditScreenViewModel

  private val noteUpsertUseCase: NoteUpsertUseCase = mockk(relaxed = true)
  private val noteListByIdsUseCase: NoteListByIdsUseCase = mockk(relaxed = true)
  private val testDispatcher = StandardTestDispatcher()
  private val testScope = TestScope(testDispatcher)

  @BeforeEach
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    viewModel = NoteEditScreenViewModel(
      noteUpsertUseCase = noteUpsertUseCase,
      noteListByIdsUseCase = noteListByIdsUseCase,
    )
  }

  @AfterEach
  fun tearDown() {
    // First advance to let any pending work complete
    testScope.advanceUntilIdle()
    // Then cancel the viewModelScope
    viewModel.viewModelScope.cancel()
    // Finally reset Main
    Dispatchers.resetMain()
  }

  // updateLocalNote tests (synchronous)

  @Test
  fun `updateLocalNote updates content`() = testScope.runTest {
    assertEquals("", viewModel.noteUiModel.value.content)

    viewModel.updateLocalNote(content = "New content")

    assertEquals("New content", viewModel.noteUiModel.value.content)
  }

  @Test
  fun `updateLocalNote updates noteType`() = testScope.runTest {
    assertEquals(BlinkoNoteType.BLINKO, viewModel.noteUiModel.value.type)

    viewModel.updateLocalNote(noteType = BlinkoNoteType.NOTE.value)

    assertEquals(BlinkoNoteType.NOTE, viewModel.noteUiModel.value.type)
  }

  @Test
  fun `updateLocalNote updates isArchived`() = testScope.runTest {
    assertEquals(false, viewModel.noteUiModel.value.isArchived)

    viewModel.updateLocalNote(isArchived = true)

    assertEquals(true, viewModel.noteUiModel.value.isArchived)
  }

  @Test
  fun `updateLocalNote does not update content when empty`() = testScope.runTest {
    viewModel.updateLocalNote(content = "Initial")
    assertEquals("Initial", viewModel.noteUiModel.value.content)

    viewModel.updateLocalNote(content = "")

    assertEquals("Initial", viewModel.noteUiModel.value.content)
  }

  @Test
  fun `updateLocalNote does not update noteType when -1`() = testScope.runTest {
    viewModel.updateLocalNote(noteType = BlinkoNoteType.NOTE.value)
    assertEquals(BlinkoNoteType.NOTE, viewModel.noteUiModel.value.type)

    viewModel.updateLocalNote(noteType = -1)

    assertEquals(BlinkoNoteType.NOTE, viewModel.noteUiModel.value.type)
  }

  // onStart tests

  @Test
  fun `onStart with noteId -1 does not fetch note`() = testScope.runTest {
    viewModel.onStart(noteId = -1, onNoteUpsert = {})
    advanceUntilIdle()
    coVerify(exactly = 0) { noteListByIdsUseCase.getNoteById(any()) }
  }

  @Test
  fun `onStart with valid noteId fetches note`() = testScope.runTest {
    val note = BlinkoNote(
      id = 42,
      content = "Test note",
      type = BlinkoNoteType.BLINKO,
      isArchived = false,
    )
    coEvery { noteListByIdsUseCase.getNoteById(42) } returns BlinkoResult.Success(note)

    viewModel.onStart(noteId = 42, onNoteUpsert = {})
    advanceUntilIdle()

    coVerify(exactly = 1) { noteListByIdsUseCase.getNoteById(42) }
  }

  // upsertNote tests

  @Test
  fun `upsertNote calls noteUpsertUseCase with current noteUiModel`() = testScope.runTest {
    viewModel.updateLocalNote(content = "Test content")
    viewModel.updateLocalNote(noteType = BlinkoNoteType.NOTE.value)

    val expectedNote = viewModel.noteUiModel.value

    coEvery { noteUpsertUseCase.upsertNote(expectedNote) } returns BlinkoResult.Success(expectedNote)

    viewModel.upsertNote()
    advanceUntilIdle()

    coVerify(exactly = 1) { noteUpsertUseCase.upsertNote(expectedNote) }
  }

  // noteTypes tests

  @Test
  fun `noteTypes contains all note types`() = testScope.runTest {
    val types = viewModel.noteTypes.value

    assertEquals(3, types.size)
    assertEquals(BlinkoNoteType.BLINKO.value, types[0])
    assertEquals(BlinkoNoteType.NOTE.value, types[1])
    assertEquals(BlinkoNoteType.TODO.value, types[2])
  }
}
