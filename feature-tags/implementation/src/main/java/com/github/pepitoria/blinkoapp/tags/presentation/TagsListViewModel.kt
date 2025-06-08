package com.github.pepitoria.blinkoapp.tags.presentation

import androidx.lifecycle.viewModelScope
import com.github.pepitoria.blinkoapp.tags.domain.GetTagsUseCase
import com.github.pepitoria.blinkoapp.ui.base.BlinkoViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TagsListViewModel @Inject constructor(
  private val getTagsUseCase: GetTagsUseCase,
) : BlinkoViewModel() {

  private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
  val isLoading = _isLoading.asStateFlow()

  private val _tags: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())
  val tags = _tags.asStateFlow()


  override fun onStart() {
    super.onStart()

    viewModelScope.launch(Dispatchers.IO) {
      _isLoading.value = true
      val tagsList = getTagsUseCase()
      _tags.value = tagsList.map { it.name }
      _isLoading.value = false
    }
  }
}