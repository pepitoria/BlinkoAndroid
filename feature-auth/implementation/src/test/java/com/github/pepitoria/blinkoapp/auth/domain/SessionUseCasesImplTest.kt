package com.github.pepitoria.blinkoapp.auth.domain

import com.github.pepitoria.blinkoapp.auth.api.domain.SessionResult
import com.github.pepitoria.blinkoapp.notes.api.domain.NoteRepository
import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNoteType
import com.github.pepitoria.blinkoapp.shared.domain.data.AuthenticationRepository
import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoResult
import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoSession
import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoUser
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SessionUseCasesImplTest {

  private lateinit var sessionUseCases: SessionUseCasesImpl

  private val noteRepository: NoteRepository = mockk()
  private val authenticationRepository: AuthenticationRepository = mockk()

  @BeforeEach
  fun setUp() {
    sessionUseCases = SessionUseCasesImpl(
      noteRepository = noteRepository,
      authenticationRepository = authenticationRepository,
    )
  }

  // logout tests

  @Test
  fun `logout delegates to authenticationRepository`() {
    every { authenticationRepository.logout() } returns Unit

    sessionUseCases.logout()

    verify(exactly = 1) { authenticationRepository.logout() }
  }

  // isSessionActive tests

  @Test
  fun `isSessionActive returns true when session exists and api call succeeds`() = runTest {
    val session = BlinkoSession(
      url = "https://test.com",
      token = "token123",
      userName = "user",
      password = "pass",
    )
    every { authenticationRepository.getSession() } returns session
    coEvery {
      noteRepository.list(
        url = session.url,
        token = session.token,
        type = BlinkoNoteType.BLINKO.value,
      )
    } returns BlinkoResult.Success(emptyList())

    val result = sessionUseCases.isSessionActive()

    assertTrue(result)
  }

  @Test
  fun `isSessionActive returns false when session exists but api call fails`() = runTest {
    val session = BlinkoSession(
      url = "https://test.com",
      token = "token123",
      userName = "user",
      password = "pass",
    )
    every { authenticationRepository.getSession() } returns session
    coEvery {
      noteRepository.list(
        url = session.url,
        token = session.token,
        type = BlinkoNoteType.BLINKO.value,
      )
    } returns BlinkoResult.Error(401, "Unauthorized")

    val result = sessionUseCases.isSessionActive()

    assertFalse(result)
  }

  @Test
  fun `isSessionActive returns false when no session exists`() = runTest {
    every { authenticationRepository.getSession() } returns null

    val result = sessionUseCases.isSessionActive()

    assertFalse(result)
  }

  // login() without parameters tests

  @Test
  fun `login without params returns success when session exists and auth succeeds`() = runTest {
    val session = BlinkoSession(
      url = "https://test.com",
      token = "token123",
      userName = "user",
      password = "pass",
    )
    val user = BlinkoUser(
      id = 1,
      name = "Test User",
      nickname = "testy",
      token = "newToken",
    )
    every { authenticationRepository.getSession() } returns session
    coEvery {
      authenticationRepository.login(
        url = session.url,
        userName = session.userName,
        password = session.password,
      )
    } returns BlinkoResult.Success(user)

    val result = sessionUseCases.login()

    assertIs<SessionResult.Success>(result)
    assertEquals("Test User", result.userName)
    assertEquals("newToken", result.token)
  }

  @Test
  fun `login without params returns error when session exists but auth fails`() = runTest {
    val session = BlinkoSession(
      url = "https://test.com",
      token = "token123",
      userName = "user",
      password = "pass",
    )
    every { authenticationRepository.getSession() } returns session
    coEvery {
      authenticationRepository.login(
        url = session.url,
        userName = session.userName,
        password = session.password,
      )
    } returns BlinkoResult.Error(401, "Invalid credentials")

    val result = sessionUseCases.login()

    assertIs<SessionResult.Error>(result)
    assertEquals(401, result.code)
    assertEquals("Invalid credentials", result.message)
  }

  @Test
  fun `login without params returns error when no session exists`() = runTest {
    every { authenticationRepository.getSession() } returns null

    val result = sessionUseCases.login()

    assertIs<SessionResult.Error>(result)
    assertEquals(-3, result.code)
    assertEquals("Missing user data, cannot login without user data", result.message)
  }

  // login(url, userName, password) tests

  @Test
  fun `login with credentials returns success and saves session when auth succeeds`() = runTest {
    val url = "https://test.com"
    val userName = "user"
    val password = "pass"
    val user = BlinkoUser(
      id = 1,
      name = "Test User",
      nickname = "testy",
      token = "newToken",
    )
    coEvery {
      authenticationRepository.login(url, userName, password)
    } returns BlinkoResult.Success(user)
    every {
      authenticationRepository.saveSession(
        url = url,
        userName = userName,
        password = password,
        token = "newToken",
      )
    } returns Unit

    val result = sessionUseCases.login(url, userName, password)

    assertIs<SessionResult.Success>(result)
    assertEquals("Test User", result.userName)
    assertEquals("newToken", result.token)
    verify(exactly = 1) {
      authenticationRepository.saveSession(
        url = url,
        userName = userName,
        password = password,
        token = "newToken",
      )
    }
  }

  @Test
  fun `login with credentials returns error when auth fails`() = runTest {
    val url = "https://test.com"
    val userName = "user"
    val password = "wrongpass"
    coEvery {
      authenticationRepository.login(url, userName, password)
    } returns BlinkoResult.Error(401, "Invalid credentials")

    val result = sessionUseCases.login(url, userName, password)

    assertIs<SessionResult.Error>(result)
    assertEquals(401, result.code)
    assertEquals("Invalid credentials", result.message)
  }

  // loginWithAccessToken tests

  @Test
  fun `loginWithAccessToken returns success when token is valid`() = runTest {
    val url = "https://test.com"
    val accessToken = "validAccessToken"
    every {
      authenticationRepository.saveSession(url = url, token = accessToken)
    } returns Unit
    coEvery {
      noteRepository.list(
        url = url,
        token = accessToken,
        type = BlinkoNoteType.BLINKO.value,
      )
    } returns BlinkoResult.Success(emptyList())

    val result = sessionUseCases.loginWithAccessToken(url, accessToken)

    assertIs<SessionResult.Success>(result)
    assertEquals("", result.userName)
    assertEquals(accessToken, result.token)
    verify(exactly = 1) {
      authenticationRepository.saveSession(url = url, token = accessToken)
    }
  }

  @Test
  fun `loginWithAccessToken returns error and clears session when token is invalid`() = runTest {
    val url = "https://test.com"
    val accessToken = "invalidAccessToken"
    every {
      authenticationRepository.saveSession(url = url, token = accessToken)
    } returns Unit
    coEvery {
      noteRepository.list(
        url = url,
        token = accessToken,
        type = BlinkoNoteType.BLINKO.value,
      )
    } returns BlinkoResult.Error(401, "Invalid token")
    every { authenticationRepository.logout() } returns Unit

    val result = sessionUseCases.loginWithAccessToken(url, accessToken)

    assertIs<SessionResult.Error>(result)
    assertEquals(401, result.code)
    assertEquals("Invalid token", result.message)
    verify(exactly = 1) { authenticationRepository.logout() }
  }
}
