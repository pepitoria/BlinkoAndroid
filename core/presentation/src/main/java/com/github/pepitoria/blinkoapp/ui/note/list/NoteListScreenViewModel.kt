package com.github.pepitoria.blinkoapp.ui.note.list

import androidx.lifecycle.viewModelScope
import com.github.pepitoria.blinkoapp.domain.NoteDeleteUseCase
import com.github.pepitoria.blinkoapp.domain.NoteListUseCase
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
class NoteListScreenViewModel @Inject constructor(
  private val noteListUseCase: NoteListUseCase,
  private val noteDeleteUseCase: NoteDeleteUseCase,
) : BlinkoViewModel() {

  private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
  val isLoading = _isLoading.asStateFlow()

  private val _notes: MutableStateFlow<List<BlinkoNote>> = MutableStateFlow(emptyList())
  val notes = _notes.asStateFlow()

  private val _noteType: MutableStateFlow<BlinkoNoteType> = MutableStateFlow(BlinkoNoteType.BLINKO)
  private val noteType = _noteType.asStateFlow()

  fun refresh() {
    onStart()
  }

  override fun onStart() {
    super.onStart()
    viewModelScope.launch(Dispatchers.IO) {
      _isLoading.value = true
      val notesResponse = noteListUseCase.listNotes(
        type = noteType.value.value
      )
      _isLoading.value = false

      when (notesResponse) {
        is BlinkoResult.Success -> {
          _notes.value = notesResponse.value
        }
        is BlinkoResult.Error -> {
          Timber.e("${this::class.java.simpleName}.onStart() error: ${notesResponse.message}")
        }
      }
    }
  }

  fun setNoteType(noteType: BlinkoNoteType) {
    _noteType.value = noteType
  }

  fun deleteNote(note: BlinkoNote) {
    note.id?.let { noteId ->
      viewModelScope.launch(Dispatchers.IO) {
        _isLoading.value = true
        val deleteResponse = noteDeleteUseCase.deleteNote(noteId)
        _isLoading.value = false

        when (deleteResponse) {
          is BlinkoResult.Success -> {
            Timber.d("Note deleted successfully: $noteId")
            _notes.value = _notes.value.filter { it.id != noteId }
//            onStart() // Refresh the list after deletion
          }
          is BlinkoResult.Error -> {
            Timber.e("${this::class.java.simpleName}.deleteNote() error: ${deleteResponse.message}")
          }
        }
      }
    }
  }
}