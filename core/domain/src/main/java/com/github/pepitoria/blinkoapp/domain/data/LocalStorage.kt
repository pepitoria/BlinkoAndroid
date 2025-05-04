package com.github.pepitoria.blinkoapp.domain.data

interface LocalStorage {
  fun saveString(key: String, value: String)
  fun getString(key: String): String?
  fun removeValue(key: String)
  fun clearAll()
}
