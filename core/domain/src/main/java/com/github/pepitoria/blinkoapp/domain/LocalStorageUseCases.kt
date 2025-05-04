package com.github.pepitoria.blinkoapp.domain

import com.github.pepitoria.blinkoapp.data.localstorage.LocalStorage
import com.github.pepitoria.blinkoapp.data.model.login.LoginRequest
import com.github.pepitoria.blinkoapp.data.model.notelist.NoteListRequest
import com.github.pepitoria.blinkoapp.data.repository.auth.AuthenticationRepository
import com.github.pepitoria.blinkoapp.data.repository.note.NoteRepository
import timber.log.Timber
import javax.inject.Inject

class LocalStorageUseCases @Inject constructor(
  private val localStorage: LocalStorage
) {
  fun saveString(key: String, value: String) {
    localStorage.saveString(key, value)
  }

  fun getString(key: String): String? {
    return localStorage.getString(key)
  }

  fun removeValue(key: String) {
    localStorage.removeValue(key)
  }

  fun clearAll() {
    localStorage.clearAll()
  }
}