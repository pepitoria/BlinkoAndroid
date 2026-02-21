package com.github.pepitoria.blinkoapp.settings.presentation

import com.github.pepitoria.blinkoapp.auth.api.domain.SessionUseCases
import com.github.pepitoria.blinkoapp.offline.connectivity.ConnectivityMonitor
import com.github.pepitoria.blinkoapp.settings.api.domain.GetDefaultTabUseCase
import com.github.pepitoria.blinkoapp.settings.api.domain.SetDefaultTabUseCase
import com.github.pepitoria.blinkoapp.settings.api.domain.Tab
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
class SettingsScreenViewModelTest {

  private lateinit var viewModel: SettingsScreenViewModel

  private val sessionUseCases: SessionUseCases = mockk(relaxed = true)
  private val getDefaultTabUseCase: GetDefaultTabUseCase = mockk(relaxed = true)
  private val setDefaultTabUseCase: SetDefaultTabUseCase = mockk(relaxed = true)
  private val connectivityMonitor: ConnectivityMonitor = mockk()
  private val isConnectedFlow = MutableStateFlow(true)

  private val testDispatcher = StandardTestDispatcher()
  private val testScope = TestScope(testDispatcher)

  @BeforeEach
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    every { connectivityMonitor.isConnected } returns isConnectedFlow
    viewModel = SettingsScreenViewModel(
      sessionUseCases = sessionUseCases,
      getDefaultTabUseCase = getDefaultTabUseCase,
      setDefaultTabUseCase = setDefaultTabUseCase,
      connectivityMonitor = connectivityMonitor,
    )
  }

  @AfterEach
  fun tearDown() {
    Dispatchers.resetMain()
  }

  // logout tests

  @Test
  fun `logout calls sessionUseCases logout`() = testScope.runTest {
    viewModel.logout()

    verify(exactly = 1) { sessionUseCases.logout() }
  }

  @Test
  fun `logout emits Exit event`() = testScope.runTest {
    var emittedEvent: SettingsScreenViewModel.Events? = null
    val job = launch {
      emittedEvent = viewModel.events.first()
    }

    viewModel.logout()
    advanceUntilIdle()

    assertEquals(SettingsScreenViewModel.Events.Exit, emittedEvent)
    job.cancel()
  }

  // onTabToShowFirstSelected tests

  @Test
  fun `onTabToShowFirstSelected saves BLINKOS tab`() = testScope.runTest {
    viewModel.onTabToShowFirstSelected("Blinkos")

    verify(exactly = 1) { setDefaultTabUseCase.setDefaultTab(Tab.BLINKOS) }
  }

  @Test
  fun `onTabToShowFirstSelected saves NOTES tab`() = testScope.runTest {
    viewModel.onTabToShowFirstSelected("Notes")

    verify(exactly = 1) { setDefaultTabUseCase.setDefaultTab(Tab.NOTES) }
  }

  @Test
  fun `onTabToShowFirstSelected saves TODOS tab`() = testScope.runTest {
    viewModel.onTabToShowFirstSelected("Todos")

    verify(exactly = 1) { setDefaultTabUseCase.setDefaultTab(Tab.TODOS) }
  }

  @Test
  fun `onTabToShowFirstSelected handles lowercase input`() = testScope.runTest {
    viewModel.onTabToShowFirstSelected("notes")

    verify(exactly = 1) { setDefaultTabUseCase.setDefaultTab(Tab.NOTES) }
  }

  @Test
  fun `onTabToShowFirstSelected handles uppercase input`() = testScope.runTest {
    viewModel.onTabToShowFirstSelected("SETTINGS")

    verify(exactly = 1) { setDefaultTabUseCase.setDefaultTab(Tab.SETTINGS) }
  }

  // getDefaultTab tests

  @Test
  fun `getDefaultTab returns formatted BLINKOS`() = testScope.runTest {
    every { getDefaultTabUseCase.getDefaultTab() } returns Tab.BLINKOS

    val result = viewModel.getDefaultTab()

    assertEquals("Blinkos", result)
  }

  @Test
  fun `getDefaultTab returns formatted NOTES`() = testScope.runTest {
    every { getDefaultTabUseCase.getDefaultTab() } returns Tab.NOTES

    val result = viewModel.getDefaultTab()

    assertEquals("Notes", result)
  }

  @Test
  fun `getDefaultTab returns formatted TODOS`() = testScope.runTest {
    every { getDefaultTabUseCase.getDefaultTab() } returns Tab.TODOS

    val result = viewModel.getDefaultTab()

    assertEquals("Todos", result)
  }

  @Test
  fun `getDefaultTab returns formatted SEARCH`() = testScope.runTest {
    every { getDefaultTabUseCase.getDefaultTab() } returns Tab.SEARCH

    val result = viewModel.getDefaultTab()

    assertEquals("Search", result)
  }

  @Test
  fun `getDefaultTab returns formatted SETTINGS`() = testScope.runTest {
    every { getDefaultTabUseCase.getDefaultTab() } returns Tab.SETTINGS

    val result = viewModel.getDefaultTab()

    assertEquals("Settings", result)
  }
}
