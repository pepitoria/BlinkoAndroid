package com.github.pepitoria.blinkoapp.notes.implementation.presentation

import androidx.lifecycle.viewModelScope
import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNote
import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNoteType
import com.github.pepitoria.blinkoapp.notes.implementation.domain.NoteDeleteUseCase
import com.github.pepitoria.blinkoapp.notes.implementation.domain.NoteListUseCase
import com.github.pepitoria.blinkoapp.notes.implementation.domain.NoteUpsertUseCase
import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoResult
import com.github.pepitoria.blinkoapp.shared.ui.base.BlinkoViewModel
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
  private val noteUpsertUseCase: NoteUpsertUseCase,
) : BlinkoViewModel() {

  private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
  val isLoading = _isLoading.asStateFlow()

  private val _notes: MutableStateFlow<List<BlinkoNote>> = MutableStateFlow(emptyList())
  val notes = _notes.asStateFlow()

  private val _archived: MutableStateFlow<List<BlinkoNote>> = MutableStateFlow(emptyList())
  val archived = _archived.asStateFlow()

  private val _noteType: MutableStateFlow<BlinkoNoteType> = MutableStateFlow(BlinkoNoteType.BLINKO)
  val noteType = _noteType.asStateFlow()

  fun refresh() {
    onStart()
    if (noteType.value == BlinkoNoteType.TODO) {
      fetchDoneTodos()
    }
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

    if (noteType == BlinkoNoteType.TODO) {
      fetchDoneTodos()
    }
  }

  private fun fetchDoneTodos() {
    viewModelScope.launch(Dispatchers.IO) {
      val notesResponse = noteListUseCase.listNotes(
        type = noteType.value.value,
        archived = true,
      )

      when (notesResponse) {
        is BlinkoResult.Success -> {
          _archived.value = notesResponse.value
        }
        is BlinkoResult.Error -> {
          Timber.e("${this::class.java.simpleName}.onStart() error: ${notesResponse.message}")
        }
      }
    }
  }

  fun deleteNote(note: BlinkoNote) {
    note.id?.let { noteId ->
      viewModelScope.launch(Dispatchers.IO) {
        val deleteResponse = noteDeleteUseCase.deleteNote(noteId)

        when (deleteResponse) {
          is BlinkoResult.Success -> {
            Timber.d("Note deleted successfully: $noteId")
            _notes.value = _notes.value.filter { it.id != noteId }
          }
          is BlinkoResult.Error -> {
            Timber.e("${this::class.java.simpleName}.deleteNote() error: ${deleteResponse.message}")
          }
        }
      }
    }
  }

  fun markNoteAsDone(note: BlinkoNote) {
    viewModelScope.launch(Dispatchers.IO) {
      val response = noteUpsertUseCase.upsertNote(
        blinkoNote = note
      )

      when (response) {
        is BlinkoResult.Success -> {
          Timber.d("${this::class.java.simpleName}.markNoteAsDone() response: ${response.value.content}")

          if (note.isArchived) {
            _notes.value = _notes.value.filter { it.id != note.id }

            val updatedList = _archived.value.toMutableList()
            updatedList.add(note)
            _archived.value = updatedList
          } else {
            _archived.value = _archived.value.filter { it.id != note.id }

            val updatedList = _notes.value.toMutableList()
            updatedList.add(note)
            _notes.value = updatedList
          }
        }

        is BlinkoResult.Error -> {
          Timber.e("${this::class.java.simpleName}.markNoteAsDone() error: ${response.message}")
        }
      }
    }

  }
}
