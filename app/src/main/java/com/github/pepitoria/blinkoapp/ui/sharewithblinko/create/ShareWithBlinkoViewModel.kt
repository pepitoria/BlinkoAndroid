package com.github.pepitoria.blinkoapp.ui.sharewithblinko.create

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import com.github.pepitoria.blinkoapp.domain.NoteUpsertUseCase
import com.github.pepitoria.blinkoapp.domain.model.BlinkoResult
import com.github.pepitoria.blinkoapp.domain.model.note.BlinkoNote
import com.github.pepitoria.blinkoapp.ui.base.BlinkoViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ShareWithBlinkoViewModel @Inject constructor(
  private val noteUpsertUseCase: NoteUpsertUseCase,
  @ApplicationContext private val appContext: Context,
) : BlinkoViewModel() {

  private val _noteCreated: MutableStateFlow<Boolean?> = MutableStateFlow(null)
  val noteCreated = _noteCreated.asStateFlow()

  fun createNote(
    content: String,
  ) {
    viewModelScope.launch(Dispatchers.IO) {
      _noteCreated.value = false
      val response = noteUpsertUseCase.upsertNote(
        blinkoNote = BlinkoNote(
          content = content,
        )
      )
      _noteCreated.value = true

      when (response) {
        is BlinkoResult.Success -> {
          Timber.d("${this::class.java.simpleName}.upsertNote() response: ${response.value.content}")
        }

        is BlinkoResult.Error -> {
          Timber.e("${this::class.java.simpleName}.upsertNote() error: ${response.message}")
          Toast.makeText(appContext, response.message, Toast.LENGTH_SHORT).show()
        }
      }
    }
  }

}