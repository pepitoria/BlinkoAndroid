package com.github.pepitoria.blinkoapp.notes.implementation.presentation

import androidx.lifecycle.viewModelScope
import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoResult
import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNote
import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNoteType
import com.github.pepitoria.blinkoapp.notes.implementation.domain.NoteUpsertUseCase
import com.github.pepitoria.blinkoapp.ui.base.BlinkoViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ShareAndEditWithBlinkoViewModel @Inject constructor(
  private val noteUpsertUseCase: NoteUpsertUseCase,
) : BlinkoViewModel() {

  private val _noteCreated: MutableStateFlow<Boolean?> = MutableStateFlow(null)
  val noteCreated = _noteCreated.asStateFlow()

  private val _error: MutableStateFlow<String?> = MutableStateFlow(null)
  val error = _error.asStateFlow()

  private val _noteUiModel: MutableStateFlow<BlinkoNote> = MutableStateFlow(BlinkoNote.EMPTY)
  val noteUiModel = _noteUiModel.asStateFlow()

  fun updateLocalNote(
    content: String,
    noteType: Int = BlinkoNoteType.BLINKO.value,
    ) {
    _noteUiModel.value = _noteUiModel.value.copy(
      content = content,
      type = BlinkoNoteType.fromResponseType(noteType),
    )
  }

  fun setNoteType(noteType: Int) {
    _noteUiModel.value = _noteUiModel.value.copy(
      type = BlinkoNoteType.fromResponseType(noteType)
    )
  }

  fun createNote() {
    viewModelScope.launch(Dispatchers.IO) {
      _noteCreated.value = false
      val response = noteUpsertUseCase.upsertNote(
        blinkoNote = noteUiModel.value
      )

      when (response) {
        is BlinkoResult.Success -> {
          _noteCreated.value = true
          Timber.d("${this::class.java.simpleName}.upsertNote() response: ${response.value.content}")
        }

        is BlinkoResult.Error -> {
          Timber.e("${this::class.java.simpleName}.upsertNote() error: ${response.code}")
          _error.value = response.message.ifEmpty { response.code.toString() }
        }
      }
    }
  }
}
