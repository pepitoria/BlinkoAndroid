package com.github.pepitoria.blinkoapp.data.localstorage

import android.content.Context
import com.github.pepitoria.blinkoapp.domain.data.LocalStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import androidx.core.content.edit

class LocalStorageSharedPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) : LocalStorage {

    companion object {
        private const val PREFERENCES_FILE_KEY = "PREFERENCES_FILE_KEY"
    }

    private fun getSharedPreferences(file: String = PREFERENCES_FILE_KEY) =
        context.getSharedPreferences(file, Context.MODE_PRIVATE)

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

    override fun saveStringSet(key: String, values: List<String>) {
        getSharedPreferences(file = "meh")
            .edit {
                putStringSet(key, values.toSet())
            }
    }

    override fun getStringSet(key: String): Set<String>? {
        val set = getSharedPreferences(file = "meh")
            .getStringSet(key, null)

        set?.let {
            // If the set is not null, return it
            return HashSet(it)
        }

        return null
    }
}