package com.github.pepitoria.blinkoapp.offline.connectivity

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

@Singleton
class ServerReachabilityMonitor @Inject constructor(
  private val connectivityMonitor: ConnectivityMonitor,
) {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

  private val isServerReachable = MutableStateFlow(true)

  private var lastUnreachableTime: Long = 0
  private var lastNetworkChangeTime: Long = 0

  val isOnline: StateFlow<Boolean> = combine(
    connectivityMonitor.isConnected,
    isServerReachable,
  ) { isConnected, isReachable ->
    val result = isConnected && isReachable
    Timber.d(
      "ServerReachabilityMonitor: " +
        "isOnline combining - " +
        "isConnected=$isConnected, " +
        "isReachable=$isReachable, " +
        "result=$result",
    )
    result
  }.stateIn(scope, SharingStarted.Eagerly, connectivityMonitor.isConnected.value)

  init {
    scope.launch {
      connectivityMonitor.isConnected.collect { isConnected ->
        onNetworkChanged(isConnected)
      }
    }
  }

  private fun onNetworkChanged(isConnected: Boolean) {
    lastNetworkChangeTime = System.currentTimeMillis()

    if (isConnected && !isServerReachable.value && isCooldownExpired()) {
      Timber.d("Network changed to connected and cooldown expired, resetting server reachability")
      isServerReachable.value = true
    }
  }

  fun reportSuccess() {
    Timber.d("ServerReachabilityMonitor: reportSuccess() called, wasReachable=${isServerReachable.value}")
    isServerReachable.value = true
    lastUnreachableTime = 0
  }

  fun reportUnreachable() {
    Timber.d("ServerReachabilityMonitor: reportUnreachable() called, wasReachable=${isServerReachable.value}")
    if (isServerReachable.value) {
      lastUnreachableTime = System.currentTimeMillis()
    }
    isServerReachable.value = false
  }

  fun shouldAttemptServerCall(): Boolean {
    val deviceConnected = connectivityMonitor.isConnected.value
    val serverReachable = isServerReachable.value
    val cooldownExpired = isCooldownExpired()

    val result = when {
      !deviceConnected -> {
        Timber.d("ServerReachabilityMonitor: shouldAttemptServerCall=false (no device connectivity)")
        false
      }
      serverReachable -> {
        Timber.d("ServerReachabilityMonitor: shouldAttemptServerCall=true (server marked reachable)")
        true
      }
      cooldownExpired -> {
        Timber.d("ServerReachabilityMonitor: shouldAttemptServerCall=true (cooldown expired)")
        true
      }
      else -> {
        Timber.d("ServerReachabilityMonitor: shouldAttemptServerCall=false (server unreachable, cooldown not expired)")
        false
      }
    }
    return result
  }

  private fun isCooldownExpired(): Boolean {
    if (lastUnreachableTime == 0L) {
      return true
    }
    val elapsed = System.currentTimeMillis() - lastUnreachableTime
    return elapsed >= COOLDOWN_MS
  }

  companion object {
    private const val COOLDOWN_MS = 30_000L
  }
}
