package com.github.pepitoria.blinkoapp.offline.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import timber.log.Timber

@Singleton
class ConnectivityMonitor @Inject constructor(
  private val context: Context,
) {
  private val connectivityManager =
    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

  private val _isConnected = MutableStateFlow(checkCurrentConnectivity())
  val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

  private var networkCallback: ConnectivityManager.NetworkCallback? = null

  init {
    startMonitoring()
  }

  private fun checkCurrentConnectivity(): Boolean {
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
      capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
  }

  private fun startMonitoring() {
    networkCallback = object : ConnectivityManager.NetworkCallback() {
      override fun onAvailable(network: Network) {
        Timber.d("Default network available")
        _isConnected.value = true
      }

      override fun onLost(network: Network) {
        Timber.d("Default network lost")
        _isConnected.value = false
      }

      override fun onCapabilitiesChanged(
        network: Network,
        networkCapabilities: NetworkCapabilities,
      ) {
        val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
          networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        Timber.d("Default network capabilities changed: hasInternet=$hasInternet")
        _isConnected.value = hasInternet
      }

      override fun onUnavailable() {
        Timber.d("Default network unavailable")
        _isConnected.value = false
      }
    }

    try {
      connectivityManager.registerDefaultNetworkCallback(networkCallback!!)
    } catch (e: Exception) {
      Timber.e(e, "Error registering default network callback")
    }
  }

  fun stopMonitoring() {
    networkCallback?.let {
      try {
        connectivityManager.unregisterNetworkCallback(it)
      } catch (e: Exception) {
        Timber.e(e, "Error unregistering network callback")
      }
      networkCallback = null
    }
  }

  fun observeConnectivity(): Flow<Boolean> = callbackFlow {
    val callback = object : ConnectivityManager.NetworkCallback() {
      override fun onAvailable(network: Network) {
        trySend(true)
      }

      override fun onLost(network: Network) {
        // When the default network is lost, we're offline
        trySend(false)
      }

      override fun onCapabilitiesChanged(
        network: Network,
        networkCapabilities: NetworkCapabilities,
      ) {
        val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
          networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        trySend(hasInternet)
      }

      override fun onUnavailable() {
        trySend(false)
      }
    }

    trySend(checkCurrentConnectivity())

    try {
      connectivityManager.registerDefaultNetworkCallback(callback)
    } catch (e: Exception) {
      Timber.e(e, "Error registering default network callback for flow")
    }

    awaitClose {
      try {
        connectivityManager.unregisterNetworkCallback(callback)
      } catch (e: Exception) {
        Timber.e(e, "Error unregistering network callback for flow")
      }
    }
  }.distinctUntilChanged()
}
