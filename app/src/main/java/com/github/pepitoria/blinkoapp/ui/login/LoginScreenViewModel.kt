package com.github.pepitoria.blinkoapp.ui.login

import com.github.pepitoria.blinkoapp.ui.base.BlinkoViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginScreenViewModel @Inject constructor(): BlinkoViewModel(){

  fun login(
    url: String,
    username: String,
    password: String) {
    Timber.d("${this::class.java.simpleName}.login() url: $url")
    Timber.d("${this::class.java.simpleName}.login() username: $username")
    Timber.d("${this::class.java.simpleName}.login() password: $password")
  }

}