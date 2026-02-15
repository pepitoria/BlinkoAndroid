package com.github.pepitoria.blinkoapp.settings.domain

import com.github.pepitoria.blinkoapp.settings.api.domain.Tab
import com.github.pepitoria.blinkoapp.settings.data.TabsRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DefaultTabUseCaseImplTest {

  private lateinit var useCase: DefaultTabUseCaseImpl

  private val tabsRepository: TabsRepository = mockk(relaxed = true)

  @BeforeEach
  fun setUp() {
    useCase = DefaultTabUseCaseImpl(
      tabsRepository = tabsRepository,
    )
  }

  @Test
  fun `getDefaultTab delegates to repository`() {
    every { tabsRepository.getDefaultTab() } returns Tab.NOTES

    val result = useCase.getDefaultTab()

    assertEquals(Tab.NOTES, result)
    verify(exactly = 1) { tabsRepository.getDefaultTab() }
  }

  @Test
  fun `getDefaultTab returns repository value for BLINKOS`() {
    every { tabsRepository.getDefaultTab() } returns Tab.BLINKOS

    val result = useCase.getDefaultTab()

    assertEquals(Tab.BLINKOS, result)
  }

  @Test
  fun `getDefaultTab returns repository value for TODOS`() {
    every { tabsRepository.getDefaultTab() } returns Tab.TODOS

    val result = useCase.getDefaultTab()

    assertEquals(Tab.TODOS, result)
  }

  @Test
  fun `setDefaultTab delegates to repository`() {
    useCase.setDefaultTab(Tab.NOTES)

    verify(exactly = 1) { tabsRepository.setDefaultTab(Tab.NOTES) }
  }

  @Test
  fun `setDefaultTab passes correct tab to repository`() {
    useCase.setDefaultTab(Tab.TODOS)

    verify(exactly = 1) { tabsRepository.setDefaultTab(Tab.TODOS) }
  }

  @Test
  fun `setDefaultTab with BLINKOS delegates correctly`() {
    useCase.setDefaultTab(Tab.BLINKOS)

    verify(exactly = 1) { tabsRepository.setDefaultTab(Tab.BLINKOS) }
  }
}
