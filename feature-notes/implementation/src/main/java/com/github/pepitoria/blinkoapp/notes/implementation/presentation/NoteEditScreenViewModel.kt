package com.github.pepitoria.blinkoapp.notes.implementation.presentation

import androidx.lifecycle.viewModelScope
import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNote
import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNoteType
import com.github.pepitoria.blinkoapp.notes.implementation.domain.NoteListByIdsUseCase
import com.github.pepitoria.blinkoapp.notes.implementation.domain.NoteUpsertUseCase
import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoResult
import com.github.pepitoria.blinkoapp.shared.ui.base.BlinkoViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@HiltViewModel
class NoteEditScreenViewModel @Inject constructor(
  private val noteUpsertUseCase: NoteUpsertUseCase,
  private val noteListByIdsUseCase: NoteListByIdsUseCase,
) : BlinkoViewModel() {

  private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
  val isLoading = _isLoading.asStateFlow()

  private val _noteUpdated: MutableStateFlow<Boolean?> = MutableStateFlow(null)
  val noteUpdated = _noteUpdated.asStateFlow()

  private val _noteUiModel: MutableStateFlow<BlinkoNote> = MutableStateFlow(BlinkoNote.EMPTY)
  val noteUiModel = _noteUiModel.asStateFlow()

  private val _error: MutableStateFlow<String?> = MutableStateFlow(null)
  val error = _error.asStateFlow()

  val noteTypes: StateFlow<List<Int>> = MutableStateFlow(
    listOf(
      BlinkoNoteType.BLINKO.value,
      BlinkoNoteType.NOTE.value,
      BlinkoNoteType.TODO.value,
    ),
  ).asStateFlow()

  private var onNoteUpsert: () -> Unit = {}

  fun onStart(
    noteId: Int = -1,
    onNoteUpsert: () -> Unit,
  ) {
    this.onNoteUpsert = onNoteUpsert

    if (noteId == -1 || noteUiModel.value != BlinkoNote.EMPTY) {
      return
    }

    viewModelScope.launch(Dispatchers.IO) {
      _isLoading.value = true
      val noteResponse = noteListByIdsUseCase.getNoteById(
        id = noteId,
      )
      _isLoading.value = false

      when (noteResponse) {
        is BlinkoResult.Success -> {
          _noteUiModel.value = noteResponse.value
        }

        is BlinkoResult.Error -> {
          Timber.e("${this::class.java.simpleName}.onStart() error: ${noteResponse.message}")
          _error.value = noteResponse.message.ifEmpty { noteResponse.code.toString() }
        }
      }
    }
  }

  fun updateLocalNote(
    content: String = "",
    noteType: Int = -1,
    isArchived: Boolean? = null,
  ) {
    if (content.isNotEmpty()) {
      _noteUiModel.value = _noteUiModel.value.copy(
        content = content,
      )
    }

    if (noteType != -1) {
      _noteUiModel.value = _noteUiModel.value.copy(
        type = BlinkoNoteType.fromResponseType(noteType),
      )
    }

    if (isArchived != null) {
      _noteUiModel.value = _noteUiModel.value.copy(
        isArchived = isArchived,
      )
    }
  }

  fun upsertNote() {
    viewModelScope.launch(Dispatchers.IO) {
      _noteUpdated.value = false
      val response = noteUpsertUseCase.upsertNote(
        blinkoNote = noteUiModel.value,
      )
      _noteUpdated.value = true

      when (response) {
        is BlinkoResult.Success -> {
          Timber.d("${this::class.java.simpleName}.upsertNote() response: ${response.value.content}")
          withContext(Dispatchers.Main) {
            onNoteUpsert()
          }
        }

        is BlinkoResult.Error -> {
          Timber.e("${this::class.java.simpleName}.upsertNote() error: ${response.message}")
          _error.value = response.message.ifEmpty { response.code.toString() }
        }
      }
    }
  }
}
