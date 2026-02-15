package com.github.pepitoria.blinkoapp.shared.storage

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocalStorageSharedPreferencesTest {

  private lateinit var localStorage: LocalStorageSharedPreferences

  @Before
  fun setUp() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    localStorage = LocalStorageSharedPreferences(context)
    localStorage.clearAll()
  }

  @After
  fun tearDown() {
    localStorage.clearAll()
  }

  // saveString tests

  @Test
  fun saveString_persistsValue() {
    localStorage.saveString("test_key", "test_value")

    val result = localStorage.getString("test_key")

    assertEquals("test_value", result)
  }

  @Test
  fun saveString_overwritesExistingValue() {
    localStorage.saveString("key", "first_value")
    localStorage.saveString("key", "second_value")

    val result = localStorage.getString("key")

    assertEquals("second_value", result)
  }

  @Test
  fun saveString_handlesEmptyValue() {
    localStorage.saveString("empty_key", "")

    val result = localStorage.getString("empty_key")

    assertEquals("", result)
  }

  @Test
  fun saveString_handlesSpecialCharacters() {
    val specialValue = "value with spaces & special chars! @#$%"
    localStorage.saveString("special_key", specialValue)

    val result = localStorage.getString("special_key")

    assertEquals(specialValue, result)
  }

  @Test
  fun saveString_handlesUnicodeCharacters() {
    val unicodeValue = "Unicode: cafe"
    localStorage.saveString("unicode_key", unicodeValue)

    val result = localStorage.getString("unicode_key")

    assertEquals(unicodeValue, result)
  }

  // getString tests

  @Test
  fun getString_retrievesStoredValue() {
    localStorage.saveString("retrieve_key", "stored_value")

    val result = localStorage.getString("retrieve_key")

    assertEquals("stored_value", result)
  }

  @Test
  fun getString_returnsNullForMissingKey() {
    val result = localStorage.getString("nonexistent_key")

    assertNull(result)
  }

  @Test
  fun getString_returnsNullAfterClearAll() {
    localStorage.saveString("clear_key", "value")
    localStorage.clearAll()

    val result = localStorage.getString("clear_key")

    assertNull(result)
  }

  // removeValue tests

  @Test
  fun removeValue_deletesKey() {
    localStorage.saveString("remove_key", "value_to_remove")

    localStorage.removeValue("remove_key")

    val result = localStorage.getString("remove_key")
    assertNull(result)
  }

  @Test
  fun removeValue_doesNotAffectOtherKeys() {
    localStorage.saveString("key1", "value1")
    localStorage.saveString("key2", "value2")

    localStorage.removeValue("key1")

    assertNull(localStorage.getString("key1"))
    assertEquals("value2", localStorage.getString("key2"))
  }

  @Test
  fun removeValue_handlesNonexistentKey() {
    // Should not throw exception
    localStorage.removeValue("nonexistent_key")

    assertNull(localStorage.getString("nonexistent_key"))
  }

  // clearAll tests

  @Test
  fun clearAll_removesAllValues() {
    localStorage.saveString("key1", "value1")
    localStorage.saveString("key2", "value2")
    localStorage.saveString("key3", "value3")

    localStorage.clearAll()

    assertNull(localStorage.getString("key1"))
    assertNull(localStorage.getString("key2"))
    assertNull(localStorage.getString("key3"))
  }

  @Test
  fun clearAll_allowsNewValuesToBeStored() {
    localStorage.saveString("old_key", "old_value")
    localStorage.clearAll()

    localStorage.saveString("new_key", "new_value")

    assertEquals("new_value", localStorage.getString("new_key"))
  }

  @Test
  fun clearAll_handlesEmptyStorage() {
    // Should not throw exception
    localStorage.clearAll()

    assertNull(localStorage.getString("any_key"))
  }

  // Edge case tests

  @Test
  fun multipleOperations_workCorrectly() {
    localStorage.saveString("key1", "value1")
    localStorage.saveString("key2", "value2")

    assertEquals("value1", localStorage.getString("key1"))

    localStorage.removeValue("key1")
    assertNull(localStorage.getString("key1"))
    assertEquals("value2", localStorage.getString("key2"))

    localStorage.saveString("key1", "new_value1")
    assertEquals("new_value1", localStorage.getString("key1"))
  }

  @Test
  fun longStringValue_isStoredAndRetrieved() {
    val longValue = "a".repeat(10000)
    localStorage.saveString("long_key", longValue)

    val result = localStorage.getString("long_key")

    assertEquals(longValue, result)
  }
}
