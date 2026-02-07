package com.github.pepitoria.blinkoapp.auth.data.repository

import com.github.pepitoria.blinkoapp.auth.data.mapper.UserMapper
import com.github.pepitoria.blinkoapp.auth.data.net.AuthApiClient
import com.github.pepitoria.blinkoapp.shared.domain.data.AuthenticationRepository
import com.github.pepitoria.blinkoapp.shared.domain.data.LocalStorage
import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoResult
import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoSession
import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoUser
import com.github.pepitoria.blinkoapp.shared.networking.mapper.toBlinkoResult
import com.github.pepitoria.blinkoapp.shared.networking.model.ApiResult
import javax.inject.Inject

private const val URL_KEY = "com.github.pepitoria.blinkoapp.data.repository.auth.URL_KEY"
private const val USERNAME_KEY = "com.github.pepitoria.blinkoapp.data.repository.auth.USERNAME_KEY"
private const val PASSWORD_KEY = "com.github.pepitoria.blinkoapp.data.repository.auth.PASSWORD_KEY"
private const val TOKEN_KEY = "com.github.pepitoria.blinkoapp.data.repository.auth.TOKEN_KEY"

class AuthenticationRepositoryImpl @Inject constructor(
  private val localStorage: LocalStorage,
  private val api: AuthApiClient,
  private val userMapper: UserMapper,
) : AuthenticationRepository {

  override fun saveSession(url: String, token: String) {
    localStorage.saveString(URL_KEY, url)
    localStorage.saveString(TOKEN_KEY, token)
  }

  override fun saveSession(url: String, userName: String, password: String, token: String) {
    localStorage.saveString(URL_KEY, url)
    localStorage.saveString(PASSWORD_KEY, password)

    if (userName.isNotEmpty()) {
      localStorage.saveString(USERNAME_KEY, userName)
    }

    if (token.isNotEmpty()) {
      localStorage.saveString(TOKEN_KEY, token)
    }

  }

  override fun getSession(): BlinkoSession? {
    val url = localStorage.getString(URL_KEY)
    val token = localStorage.getString(TOKEN_KEY)
    val userName = localStorage.getString(USERNAME_KEY)
    val password = localStorage.getString(PASSWORD_KEY)
    return if (url != null && token != null && userName != null && password != null) {
      BlinkoSession(url, token, userName, password)
    } else {
      null
    }
  }

  override suspend fun login(): BlinkoResult<BlinkoUser> {
    val url = localStorage.getString(URL_KEY)
    val userName = localStorage.getString(USERNAME_KEY)
    val password = localStorage.getString(PASSWORD_KEY)

    if (url == null || userName == null || password == null) {
      return BlinkoResult.Error.MISSING_USER_DATA
    }

    return login(url, userName, password)
  }

  override suspend fun login(
    url: String,
    userName: String,
    password: String
  ): BlinkoResult<BlinkoUser> {

    val response = api.login(
      url = url,
      userName = userName,
      password = password
    )

    return when (response) {
      is ApiResult.ApiSuccess -> {
        val userResponse = response.value
        BlinkoResult.Success(userMapper.toBlinkoUser(userResponse))
      }
      is ApiResult.ApiErrorResponse -> {
        response.toBlinkoResult()
      }
    }

  }

  override fun logout() {
    localStorage.removeValue(URL_KEY)
    localStorage.removeValue(TOKEN_KEY)
    localStorage.removeValue(USERNAME_KEY)
    localStorage.removeValue(PASSWORD_KEY)
  }
}
