package com.github.pepitoria.blinkoapp.ui.login

import androidx.lifecycle.viewModelScope
import com.github.pepitoria.blinkoapp.domain.LocalStorageUseCases
import com.github.pepitoria.blinkoapp.domain.SessionUseCases
import com.github.pepitoria.blinkoapp.domain.model.BlinkoResult
import com.github.pepitoria.blinkoapp.presentation.BuildConfig
import com.github.pepitoria.blinkoapp.ui.base.BlinkoViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginScreenViewModel @Inject constructor(
  private val localStorageUseCases: LocalStorageUseCases,
  private val sessionUseCases: SessionUseCases,
) : BlinkoViewModel() {

  sealed class Events {
    data object SessionOk : Events()
    data object InsecureConnection : Events()
  }

  private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
  val isLoading = _isLoading.asStateFlow()

  private val _isSessionActive: MutableStateFlow<Boolean> = MutableStateFlow(false)
  val isSessionActive = _isSessionActive.asStateFlow()

  private val _events = MutableSharedFlow<Events>()
  val events = _events.asSharedFlow()

  override fun onStart() {
    super.onStart()
    viewModelScope.launch(Dispatchers.IO) {
      _isLoading.value = true
      val sessionActive = sessionUseCases.isSessionActive()
      _isLoading.value = false
      Timber.d("${this::class.java.simpleName}.onStart() sessionActive: $sessionActive")
      _isSessionActive.value = sessionActive
      if (sessionActive) {
        triggerEvent(Events.SessionOk)
      } else {
        doLogin()
      }
    }
  }

  private fun doLogin() {
    viewModelScope.launch(Dispatchers.IO) {
      _isLoading.value = true
      val loginResponse = sessionUseCases.login()

      val sessionOk = loginResponse is BlinkoResult.Success
      _isLoading.value = false
      Timber.d("${this::class.java.simpleName}.login() loginOk: $sessionOk")
      _isSessionActive.value = sessionOk

      if (sessionOk) {
        triggerEvent(Events.SessionOk)
      }
    }
  }
  fun doLogin(
    url: String,
    userName: String,
    password: String,
    insecureConnectionCheck: Boolean,
  ) {
    if (url.startsWith("http://") && !insecureConnectionCheck) {
      triggerEvent(Events.InsecureConnection)
      return
    }

    viewModelScope.launch(Dispatchers.IO) {
      _isLoading.value = true
      val loginResponse = sessionUseCases.login(
        url = url,
        userName = userName,
        password = password
      )

      val sessionOk = loginResponse is BlinkoResult.Success
      _isLoading.value = false
      Timber.d("${this::class.java.simpleName}.login() loginOk: $sessionOk")
      _isSessionActive.value = sessionOk

      if (sessionOk) {
        triggerEvent(Events.SessionOk)
      }

      if (sessionOk && BuildConfig.DEBUG) {
        val user = (loginResponse as BlinkoResult.Success).value
        saveUrl(url)
        saveUserName(user.name)
        saveToken(user.token)
      }
    }

  }

  fun logout() {
    sessionUseCases.logout()
    _isSessionActive.value = false
  }

  fun getStoredUrl(): String? {
    return localStorageUseCases.getString("getStoredUrl")
  }

  fun getStoredToken(): String? {
    return localStorageUseCases.getString("getStoredToken")
  }

  fun getStoredUserName(): String? {
    return localStorageUseCases.getString("getStoredUserName")
  }

  private fun saveUrl(url: String) {
    localStorageUseCases.saveString("getStoredUrl", url)
  }

  private fun saveUserName(userName: String) {
    localStorageUseCases.saveString("getStoredUserName", userName)
  }

  private fun saveToken(token: String) {
    localStorageUseCases.saveString("getStoredToken", token)
  }

  private fun triggerEvent(event: Events) {
    viewModelScope.launch {
      _events.emit(event)
    }
  }
}