package com.github.pepitoria.blinkoapp.data.localstorage

interface LocalStorage {
  fun saveString(key: String, value: String)
  fun getString(key: String): String?
  fun removeValue(key: String)
  fun clearAll()
}
