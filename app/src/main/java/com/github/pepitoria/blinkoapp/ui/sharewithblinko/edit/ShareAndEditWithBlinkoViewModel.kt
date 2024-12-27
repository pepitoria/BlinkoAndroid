package com.github.pepitoria.blinkoapp.ui.sharewithblinko.edit

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import com.github.pepitoria.blinkoapp.domain.NoteCreateUseCase
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
class ShareAndEditWithBlinkoViewModel @Inject constructor(
  private val noteCreateUseCase: NoteCreateUseCase,
  @ApplicationContext private val appContext: Context,
) : BlinkoViewModel() {

  private val _noteCreated: MutableStateFlow<Boolean?> = MutableStateFlow(null)
  val noteCreated = _noteCreated.asStateFlow()

  private val _noteUiModel: MutableStateFlow<BlinkoNote> = MutableStateFlow(BlinkoNote.EMPTY)
  val noteUiModel = _noteUiModel.asStateFlow()

  fun updateLocalNote(
    content: String,
  ) {
    _noteUiModel.value = _noteUiModel.value.copy(
      content = content
    )
  }

  fun createNote() {
    viewModelScope.launch(Dispatchers.IO) {
      _noteCreated.value = false
      val response = noteCreateUseCase.createNote(
        content = noteUiModel.value.content
      )
      _noteCreated.value = true

      when (response) {
        is BlinkoResult.Success -> {
          Timber.d("${this::class.java.simpleName}.createNote() response: ${response.value.content}")
        }

        is BlinkoResult.Error -> {
          Timber.e("${this::class.java.simpleName}.createNote() error: ${response.message}")
          Toast.makeText(appContext, response.message, Toast.LENGTH_SHORT).show()
        }
      }
    }
  }
}