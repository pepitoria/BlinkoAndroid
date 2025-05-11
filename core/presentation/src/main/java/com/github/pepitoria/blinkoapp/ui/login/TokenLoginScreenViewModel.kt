package com.github.pepitoria.blinkoapp.ui.login

import androidx.lifecycle.viewModelScope
import com.github.pepitoria.blinkoapp.presentation.BuildConfig
import com.github.pepitoria.blinkoapp.domain.LocalStorageUseCases
import com.github.pepitoria.blinkoapp.domain.SessionUseCases
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
class TokenLoginScreenViewModel @Inject constructor(
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
      }
    }
  }

  fun checkSession(
    url: String,
    token: String,
    insecureConnectionCheck: Boolean,
  ) {
    Timber.d("${this::class.java.simpleName}.login() url: $url")
    Timber.d("${this::class.java.simpleName}.login() token: $token")

    if (url.startsWith("http://") && !insecureConnectionCheck) {
      triggerEvent(Events.InsecureConnection)
      return
    }

    viewModelScope.launch(Dispatchers.IO) {
      _isLoading.value = true
      val sessionOk = sessionUseCases.checkSession(url = url, token = token)
      _isLoading.value = false
      Timber.d("${this::class.java.simpleName}.listNotes() loginOk: $sessionOk")
      _isSessionActive.value = sessionOk

      if (sessionOk) {
        triggerEvent(Events.SessionOk)
      }

      if (sessionOk && BuildConfig.DEBUG) {
        saveUrl(url)
        saveToken(token)
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

  private fun saveUrl(url: String) {
    localStorageUseCases.saveString("getStoredUrl", url)
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