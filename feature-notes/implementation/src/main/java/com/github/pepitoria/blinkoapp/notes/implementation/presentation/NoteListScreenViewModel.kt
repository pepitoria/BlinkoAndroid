package com.github.pepitoria.blinkoapp.notes.implementation.presentation

import androidx.lifecycle.viewModelScope
import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNote
import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNoteType
import com.github.pepitoria.blinkoapp.notes.implementation.domain.NoteDeleteUseCase
import com.github.pepitoria.blinkoapp.notes.implementation.domain.NoteListUseCase
import com.github.pepitoria.blinkoapp.notes.implementation.domain.NoteUpsertUseCase
import com.github.pepitoria.blinkoapp.offline.connectivity.ConnectivityMonitor
import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoResult
import com.github.pepitoria.blinkoapp.shared.ui.base.BlinkoViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class NoteListScreenViewModel @Inject constructor(
  private val noteListUseCase: NoteListUseCase,
  private val noteDeleteUseCase: NoteDeleteUseCase,
  private val noteUpsertUseCase: NoteUpsertUseCase,
  private val connectivityMonitor: ConnectivityMonitor,
) : BlinkoViewModel() {

  private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
  val isLoading = _isLoading.asStateFlow()

  private val _notes: MutableStateFlow<List<BlinkoNote>> = MutableStateFlow(emptyList())
  val notes = _notes.asStateFlow()

  private val _archived: MutableStateFlow<List<BlinkoNote>> = MutableStateFlow(emptyList())
  val archived = _archived.asStateFlow()

  private val _noteType: MutableStateFlow<BlinkoNoteType> = MutableStateFlow(BlinkoNoteType.BLINKO)
  val noteType = _noteType.asStateFlow()

  val isConnected: StateFlow<Boolean> = connectivityMonitor.isConnected

  val pendingSyncCount: StateFlow<Int> = noteListUseCase.pendingSyncCount
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  val conflicts: StateFlow<List<BlinkoNote>> = noteListUseCase.conflicts
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _conflictToResolve: MutableStateFlow<BlinkoNote?> = MutableStateFlow(null)
  val conflictToResolve = _conflictToResolve.asStateFlow()

  init {
    observeNotesFlow()
    observeArchivedFlow()
  }

  private fun observeNotesFlow() {
    viewModelScope.launch(Dispatchers.IO) {
      noteListUseCase.listNotesAsFlow(
        type = noteType.value.value,
        archived = false,
      ).collect { noteList ->
        _notes.value = noteList
      }
    }
  }

  private fun observeArchivedFlow() {
    viewModelScope.launch(Dispatchers.IO) {
      noteListUseCase.listNotesAsFlow(
        type = noteType.value.value,
        archived = true,
      ).collect { noteList ->
        _archived.value = noteList
      }
    }
  }

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
        type = noteType.value.value,
      )
      _isLoading.value = false

      when (notesResponse) {
        is BlinkoResult.Success -> {
          _notes.value = notesResponse.value
          fetchAdditionalPagesInBackground()
        }
        is BlinkoResult.Error -> {
          Timber.e("${this::class.java.simpleName}.onStart() error: ${notesResponse.message}")
        }
      }
    }
  }

  private fun fetchAdditionalPagesInBackground() {
    viewModelScope.launch(Dispatchers.IO) {
      noteListUseCase.fetchAdditionalPages(
        type = noteType.value.value,
        archived = false,
        additionalPages = 5,
      )
    }
  }

  fun setNoteType(noteType: BlinkoNoteType) {
    val typeChanged = _noteType.value != noteType
    _noteType.value = noteType

    if (typeChanged) {
      // Re-observe with new type
      observeNotesFlow()
      if (noteType == BlinkoNoteType.TODO) {
        observeArchivedFlow()
      }
    }

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
          fetchAdditionalArchivedPagesInBackground()
        }
        is BlinkoResult.Error -> {
          Timber.e("${this::class.java.simpleName}.onStart() error: ${notesResponse.message}")
        }
      }
    }
  }

  private fun fetchAdditionalArchivedPagesInBackground() {
    viewModelScope.launch(Dispatchers.IO) {
      noteListUseCase.fetchAdditionalPages(
        type = noteType.value.value,
        archived = true,
        additionalPages = 5,
      )
    }
  }

  fun deleteNote(note: BlinkoNote) {
    viewModelScope.launch(Dispatchers.IO) {
      val deleteResponse = noteDeleteUseCase.deleteNote(note)

      when (deleteResponse) {
        is BlinkoResult.Success -> {
          Timber.d("Note deleted successfully: ${note.id ?: note.localId}")
          // Notes list will be updated via Flow observation
        }
        is BlinkoResult.Error -> {
          Timber.e("${this::class.java.simpleName}.deleteNote() error: ${deleteResponse.message}")
        }
      }
    }
  }

  fun markNoteAsDone(note: BlinkoNote) {
    viewModelScope.launch(Dispatchers.IO) {
      val response = noteUpsertUseCase.upsertNote(
        blinkoNote = note,
      )

      when (response) {
        is BlinkoResult.Success -> {
          Timber.d("${this::class.java.simpleName}.markNoteAsDone() response: ${response.value.content}")
          // Notes list will be updated via Flow observation
        }

        is BlinkoResult.Error -> {
          Timber.e("${this::class.java.simpleName}.markNoteAsDone() error: ${response.message}")
        }
      }
    }
  }

  fun showConflictDialog(note: BlinkoNote) {
    _conflictToResolve.value = note
  }

  fun dismissConflictDialog() {
    _conflictToResolve.value = null
  }

  fun resolveConflict(keepLocal: Boolean) {
    val note = _conflictToResolve.value ?: return
    viewModelScope.launch(Dispatchers.IO) {
      val result = noteListUseCase.resolveConflict(note, keepLocal)
      when (result) {
        is BlinkoResult.Success -> {
          Timber.d("Conflict resolved for note: ${note.id ?: note.localId}")
        }
        is BlinkoResult.Error -> {
          Timber.e("Failed to resolve conflict: ${result.message}")
        }
      }
      _conflictToResolve.value = null
    }
  }
}
