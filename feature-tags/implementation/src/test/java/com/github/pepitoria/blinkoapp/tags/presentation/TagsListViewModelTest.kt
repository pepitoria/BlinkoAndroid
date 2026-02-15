package com.github.pepitoria.blinkoapp.tags.presentation

import com.github.pepitoria.blinkoapp.tags.domain.BlinkoTag
import com.github.pepitoria.blinkoapp.tags.domain.GetTagsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.assertFalse
import kotlin.test.assertTrue
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
class TagsListViewModelTest {

  private lateinit var viewModel: TagsListViewModel

  private val getTagsUseCase: GetTagsUseCase = mockk(relaxed = true)

  private val testDispatcher = StandardTestDispatcher()
  private val testScope = TestScope(testDispatcher)

  @BeforeEach
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    viewModel = TagsListViewModel(
      getTagsUseCase = getTagsUseCase,
    )
  }

  @AfterEach
  fun tearDown() {
    Dispatchers.resetMain()
  }

  // onStart tests

  @Test
  fun `onStart calls getTagsUseCase`() = testScope.runTest {
    coEvery { getTagsUseCase() } returns emptyList()

    viewModel.onStart()
    advanceUntilIdle()

    coVerify(exactly = 1) { getTagsUseCase() }
  }

  @Test
  fun `onStart calls getTagsUseCase with tags`() = testScope.runTest {
    val tags = listOf(
      BlinkoTag(name = "Tag1"),
      BlinkoTag(name = "Tag2"),
      BlinkoTag(name = "Tag3"),
    )
    coEvery { getTagsUseCase() } returns tags

    viewModel.onStart()
    advanceUntilIdle()

    coVerify(exactly = 1) { getTagsUseCase() }
  }

  @Test
  fun `onStart with single tag calls getTagsUseCase`() = testScope.runTest {
    coEvery { getTagsUseCase() } returns listOf(BlinkoTag(name = "SingleTag"))

    viewModel.onStart()
    advanceUntilIdle()

    coVerify(exactly = 1) { getTagsUseCase() }
  }

  @Test
  fun `onStart with multiple tags calls getTagsUseCase`() = testScope.runTest {
    val tags = listOf(
      BlinkoTag(name = "work"),
      BlinkoTag(name = "personal"),
      BlinkoTag(name = "urgent"),
    )
    coEvery { getTagsUseCase() } returns tags

    viewModel.onStart()
    advanceUntilIdle()

    coVerify(exactly = 1) { getTagsUseCase() }
  }

  @Test
  fun `initial tags state is empty list`() = testScope.runTest {
    assertTrue(viewModel.tags.value.isEmpty())
  }

  @Test
  fun `initial isLoading state is false`() = testScope.runTest {
    assertFalse(viewModel.isLoading.value)
  }
}
