package com.github.pepitoria.blinkoapp.data.localstorage

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.Serializable
import javax.inject.Inject

class LocalStorageSharedPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) : LocalStorage {

    companion object {
        private const val PREFERENCES_FILE_KEY = "PREFERENCES_FILE_KEY"
    }

    private fun getSharedPreferences() =
        context.getSharedPreferences(PREFERENCES_FILE_KEY, Context.MODE_PRIVATE)

    override fun saveString(key: String, value: String) {
        getSharedPreferences()
            .edit()
            .putString(key, value)
            .apply()
    }

    override fun removeValue(key: String) {
        getSharedPreferences().edit().remove(key).apply()
    }

    override fun getString(key: String): String? {
        return getSharedPreferences()
            .getString(key, null)
    }

    override fun clearAll() {
        getSharedPreferences().edit().clear().apply()
    }
}