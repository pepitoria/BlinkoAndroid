package com.github.pepitoria.blinkoapp.tags

import com.github.pepitoria.blinkoapp.ui.base.BlinkoViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class TagsListViewModel @Inject constructor(
) : BlinkoViewModel() {

  private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
  val isLoading = _isLoading.asStateFlow()

  private val _tags: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())
  val tags = _tags.asStateFlow()


  override fun onStart() {
    super.onStart()

    _tags.value = listOf(
      "pending",
      "selfhosting",
      "regalos",
      "Important",
      "Shopping",
      "Ideas",
      "Health",
      "Travel",
      "Finance",
      "Family"
    )
  }
}