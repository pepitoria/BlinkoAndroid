package com.github.pepitoria.blinkoapp.ui.note.edit

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.github.pepitoria.blinkoapp.domain.NoteListByIdsUseCase
import com.github.pepitoria.blinkoapp.domain.NoteUpsertUseCase
import com.github.pepitoria.blinkoapp.domain.model.BlinkoResult
import com.github.pepitoria.blinkoapp.domain.model.note.BlinkoNote
import com.github.pepitoria.blinkoapp.domain.model.note.BlinkoNoteType
import com.github.pepitoria.blinkoapp.ui.base.BlinkoViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class NoteEditScreenViewModel @Inject constructor(
  private val noteUpsertUseCase: NoteUpsertUseCase,
  private val noteListByIdsUseCase: NoteListByIdsUseCase,
  @ApplicationContext private val appContext: Context,
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
    )
  ).asStateFlow()

  private var onNoteUpsert: () -> Unit = {}

  fun onStart(noteId: Int = -1, onNoteUpsert: () -> Unit) {
    this.onNoteUpsert = onNoteUpsert

    if (noteId == -1 || noteUiModel.value != BlinkoNote.EMPTY) {
      return
    }

    viewModelScope.launch(Dispatchers.IO) {
      _isLoading.value = true
      val noteResponse = noteListByIdsUseCase.getNoteById(
        id = noteId
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
  ) {

    if (content.isNotEmpty()) {
      _noteUiModel.value = _noteUiModel.value.copy(
        content = content,
      )
    }

    if (noteType != -1) {
      _noteUiModel.value = _noteUiModel.value.copy(
        type = BlinkoNoteType.fromResponseType(noteType)
      )
    }
  }

  fun upsertNote() {
    viewModelScope.launch(Dispatchers.IO) {
      _noteUpdated.value = false
      val response = noteUpsertUseCase.upsertNote(
        blinkoNote = noteUiModel.value
      )
      _noteUpdated.value = true

      when (response) {
        is BlinkoResult.Success -> {
          Timber.d("${this::class.java.simpleName}.upsertNote() response: ${response.value.content}")
          viewModelScope.launch(Dispatchers.Main) {
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