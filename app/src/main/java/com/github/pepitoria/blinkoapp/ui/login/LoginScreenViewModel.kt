package com.github.pepitoria.blinkoapp.ui.login

import androidx.lifecycle.viewModelScope
import com.github.pepitoria.blinkoapp.domain.LoginUseCase
import com.github.pepitoria.blinkoapp.ui.base.BlinkoViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginScreenViewModel @Inject constructor(
  private val loginUseCase: LoginUseCase
) : BlinkoViewModel() {

  fun login(
    url: String,
    username: String,
    password: String
  ) {
    Timber.d("${this::class.java.simpleName}.login() url: $url")
    Timber.d("${this::class.java.simpleName}.login() username: $username")
    Timber.d("${this::class.java.simpleName}.login() password: $password")

    viewModelScope.launch(Dispatchers.IO) {
      val loginOk = loginUseCase.login(url, username, password)
      Timber.d("${this::class.java.simpleName}.login() loginOk: $loginOk")
    }
  }

}