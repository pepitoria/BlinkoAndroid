package com.github.pepitoria.blinkoapp.auth.data.repository

import com.github.pepitoria.blinkoapp.auth.data.mapper.UserMapper
import com.github.pepitoria.blinkoapp.auth.data.model.LoginResponse
import com.github.pepitoria.blinkoapp.auth.data.net.AuthApiClient
import com.github.pepitoria.blinkoapp.shared.domain.data.LocalStorage
import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoResult
import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoUser
import com.github.pepitoria.blinkoapp.shared.networking.model.ApiResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private const val URL_KEY = "com.github.pepitoria.blinkoapp.data.repository.auth.URL_KEY"
private const val USERNAME_KEY = "com.github.pepitoria.blinkoapp.data.repository.auth.USERNAME_KEY"
private const val PASSWORD_KEY = "com.github.pepitoria.blinkoapp.data.repository.auth.PASSWORD_KEY"
private const val TOKEN_KEY = "com.github.pepitoria.blinkoapp.data.repository.auth.TOKEN_KEY"

class AuthenticationRepositoryImplTest {

  private lateinit var authenticationRepository: AuthenticationRepositoryImpl

  private val localStorage: LocalStorage = mockk(relaxed = true)
  private val api: AuthApiClient = mockk()
  private val userMapper: UserMapper = mockk()

  @BeforeEach
  fun setUp() {
    authenticationRepository = AuthenticationRepositoryImpl(
      localStorage = localStorage,
      api = api,
      userMapper = userMapper,
    )
  }

  // saveSession(url, token) tests

  @Test
  fun `saveSession with url and token saves both values to localStorage`() {
    val url = "https://test.com"
    val token = "testToken"

    authenticationRepository.saveSession(url = url, token = token)

    verify(exactly = 1) { localStorage.saveString(URL_KEY, url) }
    verify(exactly = 1) { localStorage.saveString(TOKEN_KEY, token) }
  }

  // saveSession(url, userName, password, token) tests

  @Test
  fun `saveSession with full credentials saves all values when not empty`() {
    val url = "https://test.com"
    val userName = "user"
    val password = "pass"
    val token = "testToken"

    authenticationRepository.saveSession(
      url = url,
      userName = userName,
      password = password,
      token = token,
    )

    verify(exactly = 1) { localStorage.saveString(URL_KEY, url) }
    verify(exactly = 1) { localStorage.saveString(PASSWORD_KEY, password) }
    verify(exactly = 1) { localStorage.saveString(USERNAME_KEY, userName) }
    verify(exactly = 1) { localStorage.saveString(TOKEN_KEY, token) }
  }

  @Test
  fun `saveSession with empty userName does not save userName`() {
    val url = "https://test.com"
    val userName = ""
    val password = "pass"
    val token = "testToken"

    authenticationRepository.saveSession(
      url = url,
      userName = userName,
      password = password,
      token = token,
    )

    verify(exactly = 1) { localStorage.saveString(URL_KEY, url) }
    verify(exactly = 1) { localStorage.saveString(PASSWORD_KEY, password) }
    verify(exactly = 0) { localStorage.saveString(USERNAME_KEY, any()) }
    verify(exactly = 1) { localStorage.saveString(TOKEN_KEY, token) }
  }

  @Test
  fun `saveSession with empty token does not save token`() {
    val url = "https://test.com"
    val userName = "user"
    val password = "pass"
    val token = ""

    authenticationRepository.saveSession(
      url = url,
      userName = userName,
      password = password,
      token = token,
    )

    verify(exactly = 1) { localStorage.saveString(URL_KEY, url) }
    verify(exactly = 1) { localStorage.saveString(PASSWORD_KEY, password) }
    verify(exactly = 1) { localStorage.saveString(USERNAME_KEY, userName) }
    verify(exactly = 0) { localStorage.saveString(TOKEN_KEY, any()) }
  }

  // getSession tests

  @Test
  fun `getSession returns session when url and token exist in localStorage`() {
    every { localStorage.getString(URL_KEY) } returns "https://test.com"
    every { localStorage.getString(TOKEN_KEY) } returns "testToken"
    every { localStorage.getString(USERNAME_KEY) } returns "user"
    every { localStorage.getString(PASSWORD_KEY) } returns "pass"

    val session = authenticationRepository.getSession()

    assertNotNull(session)
    assertEquals("https://test.com", session.url)
    assertEquals("testToken", session.token)
    assertEquals("user", session.userName)
    assertEquals("pass", session.password)
  }

  @Test
  fun `getSession returns session with empty userName and password when not stored`() {
    every { localStorage.getString(URL_KEY) } returns "https://test.com"
    every { localStorage.getString(TOKEN_KEY) } returns "testToken"
    every { localStorage.getString(USERNAME_KEY) } returns null
    every { localStorage.getString(PASSWORD_KEY) } returns null

    val session = authenticationRepository.getSession()

    assertNotNull(session)
    assertEquals("https://test.com", session.url)
    assertEquals("testToken", session.token)
    assertEquals("", session.userName)
    assertEquals("", session.password)
  }

  @Test
  fun `getSession returns null when url is missing`() {
    every { localStorage.getString(URL_KEY) } returns null
    every { localStorage.getString(TOKEN_KEY) } returns "testToken"

    val session = authenticationRepository.getSession()

    assertNull(session)
  }

  @Test
  fun `getSession returns null when token is missing`() {
    every { localStorage.getString(URL_KEY) } returns "https://test.com"
    every { localStorage.getString(TOKEN_KEY) } returns null

    val session = authenticationRepository.getSession()

    assertNull(session)
  }

  // login() without parameters tests

  @Test
  fun `login without params returns error when url is missing`() = runTest {
    every { localStorage.getString(URL_KEY) } returns null
    every { localStorage.getString(USERNAME_KEY) } returns "user"
    every { localStorage.getString(PASSWORD_KEY) } returns "pass"

    val result = authenticationRepository.login()

    assertEquals(BlinkoResult.Error.MISSING_USER_DATA, result)
  }

  @Test
  fun `login without params returns error when userName is missing`() = runTest {
    every { localStorage.getString(URL_KEY) } returns "https://test.com"
    every { localStorage.getString(USERNAME_KEY) } returns null
    every { localStorage.getString(PASSWORD_KEY) } returns "pass"

    val result = authenticationRepository.login()

    assertEquals(BlinkoResult.Error.MISSING_USER_DATA, result)
  }

  @Test
  fun `login without params returns error when password is missing`() = runTest {
    every { localStorage.getString(URL_KEY) } returns "https://test.com"
    every { localStorage.getString(USERNAME_KEY) } returns "user"
    every { localStorage.getString(PASSWORD_KEY) } returns null

    val result = authenticationRepository.login()

    assertEquals(BlinkoResult.Error.MISSING_USER_DATA, result)
  }

  @Test
  fun `login without params calls login with stored credentials when all present`() = runTest {
    val url = "https://test.com"
    val userName = "user"
    val password = "pass"
    val loginResponse = LoginResponse(
      id = 1,
      name = "Test User",
      nickname = "testy",
      token = "newToken",
    )
    val blinkoUser = BlinkoUser(
      id = 1,
      name = "Test User",
      nickname = "testy",
      token = "newToken",
    )
    every { localStorage.getString(URL_KEY) } returns url
    every { localStorage.getString(USERNAME_KEY) } returns userName
    every { localStorage.getString(PASSWORD_KEY) } returns password
    coEvery { api.login(url, userName, password) } returns ApiResult.ApiSuccess(loginResponse)
    every { userMapper.toBlinkoUser(loginResponse) } returns blinkoUser

    val result = authenticationRepository.login()

    assertIs<BlinkoResult.Success<BlinkoUser>>(result)
    assertEquals(blinkoUser, result.value)
  }

  // login(url, userName, password) tests

  @Test
  fun `login with credentials returns success when api succeeds`() = runTest {
    val url = "https://test.com"
    val userName = "user"
    val password = "pass"
    val loginResponse = LoginResponse(
      id = 1,
      name = "Test User",
      nickname = "testy",
      token = "newToken",
    )
    val blinkoUser = BlinkoUser(
      id = 1,
      name = "Test User",
      nickname = "testy",
      token = "newToken",
    )
    coEvery { api.login(url, userName, password) } returns ApiResult.ApiSuccess(loginResponse)
    every { userMapper.toBlinkoUser(loginResponse) } returns blinkoUser

    val result = authenticationRepository.login(url, userName, password)

    assertIs<BlinkoResult.Success<BlinkoUser>>(result)
    assertEquals(blinkoUser, result.value)
    coVerify(exactly = 1) { api.login(url, userName, password) }
    verify(exactly = 1) { userMapper.toBlinkoUser(loginResponse) }
  }

  @Test
  fun `login with credentials returns error when api fails`() = runTest {
    val url = "https://test.com"
    val userName = "user"
    val password = "wrongpass"
    coEvery {
      api.login(url, userName, password)
    } returns ApiResult.ApiErrorResponse(code = 401, message = "Invalid credentials")

    val result = authenticationRepository.login(url, userName, password)

    assertIs<BlinkoResult.Error>(result)
    assertEquals(401, result.code)
    assertEquals("Invalid credentials", result.message)
  }

  // logout tests

  @Test
  fun `logout removes all stored values from localStorage`() {
    authenticationRepository.logout()

    verify(exactly = 1) { localStorage.removeValue(URL_KEY) }
    verify(exactly = 1) { localStorage.removeValue(TOKEN_KEY) }
    verify(exactly = 1) { localStorage.removeValue(USERNAME_KEY) }
    verify(exactly = 1) { localStorage.removeValue(PASSWORD_KEY) }
  }
}
