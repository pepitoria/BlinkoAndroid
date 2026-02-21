package com.github.pepitoria.blinkoapp.offline.sync

import android.content.Context
import com.github.pepitoria.blinkoapp.offline.connectivity.ConnectivityMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@Singleton
class SyncScheduler @Inject constructor(
  @ApplicationContext private val context: Context,
  private val connectivityMonitor: ConnectivityMonitor,
  private val syncQueueManager: SyncQueueManager,
) {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
  private var isObserving = false

  fun startObserving() {
    if (isObserving) return
    isObserving = true

    Timber.d("SyncScheduler: Starting connectivity observation")

    scope.launch {
      connectivityMonitor.isConnected
        .collectLatest { isConnected ->
          Timber.d("SyncScheduler: Connectivity changed to $isConnected")
          if (isConnected) {
            triggerSyncIfNeeded()
          }
        }
    }
  }

  private suspend fun triggerSyncIfNeeded() {
    if (syncQueueManager.hasPendingOperations()) {
      Timber.d("SyncScheduler: Pending operations found, triggering sync")
      SyncWorker.enqueue(context)
    } else {
      Timber.d("SyncScheduler: No pending operations")
    }
  }

  fun triggerSync() {
    scope.launch {
      triggerSyncIfNeeded()
    }
  }
}
