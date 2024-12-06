package com.github.pepitoria.blinkoapp.ui.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.jvm.java

abstract class BlinkoViewModel(
) : ViewModel(), ViewModelComposableEvents {

    override fun onStart() {
      Timber.d("${this::class.java.simpleName}.onStart()")
    }

    override fun onStop() {
      Timber.d("${this::class.java.simpleName}.onStop()")
    }
}