package com.github.pepitoria.blinkoapp.search.implementation

import com.github.pepitoria.blinkoapp.notes.api.domain.NoteSearchUseCase
import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNote
import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNoteType
import com.github.pepitoria.blinkoapp.offline.connectivity.ConnectivityMonitor
import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
class SearchScreenViewModelTest {

  private lateinit var viewModel: SearchScreenViewModel

  private val searchUseCase: NoteSearchUseCase = mockk(relaxed = true)
  private val connectivityMonitor: ConnectivityMonitor = mockk()
  private val isConnectedFlow = MutableStateFlow(true)

  private val testDispatcher = StandardTestDispatcher()
  private val testScope = TestScope(testDispatcher)

  @BeforeEach
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    every { connectivityMonitor.isConnected } returns isConnectedFlow
    viewModel = SearchScreenViewModel(
      searchUseCase = searchUseCase,
      connectivityMonitor = connectivityMonitor,
    )
  }

  @AfterEach
  fun tearDown() {
    Dispatchers.resetMain()
  }

  // search tests

  @Test
  fun `search with blank query clears notes and does not call use case`() = testScope.runTest {
    viewModel.search("")
    advanceUntilIdle()

    assertTrue(viewModel.notes.value.isEmpty())
    coVerify(exactly = 0) { searchUseCase.searchNotes(any()) }
  }

  @Test
  fun `search updates query state`() = testScope.runTest {
    assertEquals("", viewModel.query.value)

    viewModel.search("test query")

    assertEquals("test query", viewModel.query.value)
  }

  @Test
  fun `search with valid query calls searchUseCase`() = testScope.runTest {
    val notes = listOf(
      BlinkoNote(
        id = 1,
        content = "Test note",
        type = BlinkoNoteType.BLINKO,
        isArchived = false,
      ),
    )
    coEvery { searchUseCase.searchNotes("test") } returns BlinkoResult.Success(notes)

    viewModel.search("test")
    advanceUntilIdle()

    coVerify(exactly = 1) { searchUseCase.searchNotes("test") }
  }

  @Test
  fun `search with whitespace only query clears notes`() = testScope.runTest {
    viewModel.search("   ")
    advanceUntilIdle()

    assertTrue(viewModel.notes.value.isEmpty())
    coVerify(exactly = 0) { searchUseCase.searchNotes(any()) }
  }

  @Test
  fun `initial state has empty notes and query`() {
    assertTrue(viewModel.notes.value.isEmpty())
    assertEquals("", viewModel.query.value)
    assertEquals(false, viewModel.isLoading.value)
  }
}
