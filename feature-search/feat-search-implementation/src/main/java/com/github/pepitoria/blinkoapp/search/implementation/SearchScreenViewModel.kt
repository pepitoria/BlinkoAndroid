package com.github.pepitoria.blinkoapp.search.implementation

import androidx.lifecycle.viewModelScope
import com.github.pepitoria.blinkoapp.domain.NoteListUseCase
import com.github.pepitoria.blinkoapp.domain.NoteSearchUseCase
import com.github.pepitoria.blinkoapp.domain.model.BlinkoResult
import com.github.pepitoria.blinkoapp.domain.model.note.BlinkoNote
import com.github.pepitoria.blinkoapp.ui.base.BlinkoViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SearchScreenViewModel @Inject constructor(
  private val searchUseCase: NoteSearchUseCase,
) : BlinkoViewModel() {

  private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
  val isLoading = _isLoading.asStateFlow()

  private val _notes: MutableStateFlow<List<BlinkoNote>> = MutableStateFlow(emptyList())
  val notes = _notes.asStateFlow()

  private val _query: MutableStateFlow<String> = MutableStateFlow("")
  val query = _query.asStateFlow()

  fun search(query: String) {
    _query.value = query

    viewModelScope.launch(Dispatchers.IO) {
      _isLoading.value = true
      val notesResponse = searchUseCase.searchNotes(
        searchTerm = query
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
}