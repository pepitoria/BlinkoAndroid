package com.github.pepitoria.blinkoapp.ui.sharewithblinko.edit

import androidx.lifecycle.viewModelScope
import com.github.pepitoria.blinkoapp.domain.NoteUpsertUseCase
import com.github.pepitoria.blinkoapp.domain.model.BlinkoResult
import com.github.pepitoria.blinkoapp.domain.model.note.BlinkoNote
import com.github.pepitoria.blinkoapp.domain.model.note.BlinkoNoteType
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
  ) {
    _noteUiModel.value = _noteUiModel.value.copy(
      content = content
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
        blinkoNote = BlinkoNote(
          content = noteUiModel.value.content,
          type = noteUiModel.value.type,
        )
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