package com.github.pepitoria.blinkoapp.settings.data

import com.github.pepitoria.blinkoapp.settings.api.domain.Tab
import com.github.pepitoria.blinkoapp.shared.domain.data.LocalStorage
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TabsRepositoryImplTest {

  private lateinit var tabsRepository: TabsRepositoryImpl

  private val localStorage: LocalStorage = mockk(relaxed = true)

  @BeforeEach
  fun setUp() {
    tabsRepository = TabsRepositoryImpl(
      localStorage = localStorage,
    )
  }

  @Test
  fun `getDefaultTab returns stored tab when value exists`() {
    every { localStorage.getString(any()) } returns "NOTES"

    val result = tabsRepository.getDefaultTab()

    assertEquals(Tab.NOTES, result)
  }

  @Test
  fun `getDefaultTab returns BLINKOS when no value stored`() {
    every { localStorage.getString(any()) } returns null

    val result = tabsRepository.getDefaultTab()

    assertEquals(Tab.BLINKOS, result)
  }

  @Test
  fun `getDefaultTab returns TODOS when stored`() {
    every { localStorage.getString(any()) } returns "TODOS"

    val result = tabsRepository.getDefaultTab()

    assertEquals(Tab.TODOS, result)
  }

  @Test
  fun `setDefaultTab saves tab name to localStorage`() {
    tabsRepository.setDefaultTab(Tab.NOTES)

    verify(exactly = 1) {
      localStorage.saveString(
        key = "com.github.pepitoria.blinkoapp.settings.data.default_tab",
        value = "NOTES",
      )
    }
  }

  @Test
  fun `setDefaultTab saves TODOS tab correctly`() {
    tabsRepository.setDefaultTab(Tab.TODOS)

    verify(exactly = 1) {
      localStorage.saveString(
        key = "com.github.pepitoria.blinkoapp.settings.data.default_tab",
        value = "TODOS",
      )
    }
  }

  @Test
  fun `setDefaultTab saves BLINKOS tab correctly`() {
    tabsRepository.setDefaultTab(Tab.BLINKOS)

    verify(exactly = 1) {
      localStorage.saveString(
        key = "com.github.pepitoria.blinkoapp.settings.data.default_tab",
        value = "BLINKOS",
      )
    }
  }
}
