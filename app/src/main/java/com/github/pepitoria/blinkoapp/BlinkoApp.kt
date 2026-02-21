package com.github.pepitoria.blinkoapp

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.github.pepitoria.blinkoapp.offline.sync.SyncScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import timber.log.Timber

const val TAG_PREFIX = "Blinko - "

@HiltAndroidApp
class BlinkoApp : Application(), Configuration.Provider {

  @Inject
  lateinit var workerFactory: HiltWorkerFactory

  @Inject
  lateinit var syncScheduler: SyncScheduler

  override fun onCreate() {
    super.onCreate()
    initTimber()
    initSyncScheduler()
  }

  private fun initSyncScheduler() {
    syncScheduler.startObserving()
    Timber.d("SyncScheduler initialized")
  }

  override val workManagerConfiguration: Configuration
    get() = Configuration.Builder()
      .setWorkerFactory(workerFactory)
      .setMinimumLoggingLevel(if (BuildConfig.DEBUG) android.util.Log.DEBUG else android.util.Log.INFO)
      .build()

  private fun initTimber() {
    if (BuildConfig.DEBUG) {
      Timber.plant(object : Timber.DebugTree() {
        override fun log(
          priority: Int,
          tag: String?,
          message: String,
          t: Throwable?,
        ) {
          super.log(priority, "$TAG_PREFIX$tag", message, t)
        }
      })
    }
  }
}
