package com.github.pepitoria.blinkoapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

const val TAG_PREFIX = "Blinko - "

@HiltAndroidApp
class BlinkoApp: Application() {

  override fun onCreate() {
    super.onCreate()
    initTimber()
  }

  private fun initTimber() {
    if (BuildConfig.DEBUG) {
      Timber.plant(object : Timber.DebugTree() {
        override fun log(
          priority: Int, tag: String?, message: String, t: Throwable?
        ) {
          super.log(priority, "$TAG_PREFIX$tag", message, t)
        }
      })
    }
  }
}