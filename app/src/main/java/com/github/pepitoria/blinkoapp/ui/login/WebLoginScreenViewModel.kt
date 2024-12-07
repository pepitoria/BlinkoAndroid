package com.github.pepitoria.blinkoapp.ui.login

import androidx.lifecycle.viewModelScope
import com.github.pepitoria.blinkoapp.domain.LoginUseCase
import com.github.pepitoria.blinkoapp.ui.base.BlinkoViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class WebLoginScreenViewModel @Inject constructor(
): BlinkoViewModel(){

  private val _url: MutableStateFlow<String?> = MutableStateFlow(null)
  val url = _url.asStateFlow()

  fun setUrl(url: String){
    if (url.startsWith("http")){
      _url.value = url
      Timber.d("URL: $url")
    } else {
      _url.value = "https://$url"
      Timber.d("URL: ${_url.value}")
    }
  }
}