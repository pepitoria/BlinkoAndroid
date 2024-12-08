package com.github.pepitoria.blinkoapp.ui.login

import androidx.lifecycle.viewModelScope
import com.github.pepitoria.blinkoapp.domain.LocalStorageUseCases
import com.github.pepitoria.blinkoapp.domain.LoginUseCase
import com.github.pepitoria.blinkoapp.domain.NoteListUseCase
import com.github.pepitoria.blinkoapp.domain.SessionUseCases
import com.github.pepitoria.blinkoapp.ui.base.BlinkoViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TokenLoginScreenViewModel @Inject constructor(
  private val localStorageUseCases: LocalStorageUseCases,
  private val sessionUseCases: SessionUseCases,
): BlinkoViewModel(){

  fun login(
    url: String,
    token: String) {
    Timber.d("${this::class.java.simpleName}.login() url: $url")
    Timber.d("${this::class.java.simpleName}.login() token: $token")

    viewModelScope.launch(Dispatchers.IO) {
      val sessionOk = sessionUseCases.checkSession(url = url, token = token)
      Timber.d("${this::class.java.simpleName}.listNotes() loginOk: $sessionOk")

      if (sessionOk) {
        saveUrl(url)
        saveToken(token)
      }
    }
  }

  fun getStoredUrl(): String? {
    return localStorageUseCases.getString("getStoredUrl")
  }

  fun getStoredToken(): String? {
    return localStorageUseCases.getString("getStoredToken")
  }

  fun saveUrl(url: String) {
    localStorageUseCases.saveString("getStoredUrl", url)
  }

  fun saveToken(token: String) {
    localStorageUseCases.saveString("getStoredToken", token)
  }
}