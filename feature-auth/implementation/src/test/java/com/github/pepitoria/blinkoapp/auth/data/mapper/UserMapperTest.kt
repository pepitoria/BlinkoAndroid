package com.github.pepitoria.blinkoapp.auth.data.mapper

import com.github.pepitoria.blinkoapp.auth.data.model.LoginResponse
import kotlin.test.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UserMapperTest {

  private lateinit var userMapper: UserMapper

  @BeforeEach
  fun setUp() {
    userMapper = UserMapper()
  }

  @Test
  fun `toBlinkoUser maps all fields correctly`() {
    val loginResponse = LoginResponse(
      id = 1,
      name = "John Doe",
      nickname = "johnd",
      token = "abc123token",
    )

    val result = userMapper.toBlinkoUser(loginResponse)

    assertEquals(1, result.id)
    assertEquals("John Doe", result.name)
    assertEquals("johnd", result.nickname)
    assertEquals("abc123token", result.token)
  }

  @Test
  fun `toBlinkoUser handles null id`() {
    val loginResponse = LoginResponse(
      id = null,
      name = "Test User",
      nickname = "test",
      token = "token",
    )

    val result = userMapper.toBlinkoUser(loginResponse)

    assertEquals(0, result.id)
  }

  @Test
  fun `toBlinkoUser handles null name`() {
    val loginResponse = LoginResponse(
      id = 2,
      name = null,
      nickname = "nick",
      token = "token",
    )

    val result = userMapper.toBlinkoUser(loginResponse)

    assertEquals("", result.name)
  }

  @Test
  fun `toBlinkoUser handles null nickname`() {
    val loginResponse = LoginResponse(
      id = 3,
      name = "User",
      nickname = null,
      token = "token",
    )

    val result = userMapper.toBlinkoUser(loginResponse)

    assertEquals("", result.nickname)
  }

  @Test
  fun `toBlinkoUser handles null token`() {
    val loginResponse = LoginResponse(
      id = 4,
      name = "User",
      nickname = "nick",
      token = null,
    )

    val result = userMapper.toBlinkoUser(loginResponse)

    assertEquals("", result.token)
  }

  @Test
  fun `toBlinkoUser handles all null fields`() {
    val loginResponse = LoginResponse(
      id = null,
      name = null,
      nickname = null,
      token = null,
    )

    val result = userMapper.toBlinkoUser(loginResponse)

    assertEquals(0, result.id)
    assertEquals("", result.name)
    assertEquals("", result.nickname)
    assertEquals("", result.token)
  }

  @Test
  fun `toBlinkoUser ignores extra fields in response`() {
    val loginResponse = LoginResponse(
      id = 5,
      name = "Test",
      nickname = "tester",
      role = "admin",
      token = "secret",
      image = "http://example.com/image.png",
      loginType = "password",
    )

    val result = userMapper.toBlinkoUser(loginResponse)

    assertEquals(5, result.id)
    assertEquals("Test", result.name)
    assertEquals("tester", result.nickname)
    assertEquals("secret", result.token)
  }
}
