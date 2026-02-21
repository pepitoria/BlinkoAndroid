package com.github.pepitoria.blinkoapp.offline.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
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
    val networkRequest = NetworkRequest.Builder()
      .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
      .build()

    networkCallback = object : ConnectivityManager.NetworkCallback() {
      override fun onAvailable(network: Network) {
        Timber.d("Network available")
        _isConnected.value = true
      }

      override fun onLost(network: Network) {
        Timber.d("Network lost")
        _isConnected.value = checkCurrentConnectivity()
      }

      override fun onCapabilitiesChanged(
        network: Network,
        networkCapabilities: NetworkCapabilities,
      ) {
        val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
          networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        Timber.d("Network capabilities changed: hasInternet=$hasInternet")
        _isConnected.value = hasInternet
      }

      override fun onUnavailable() {
        Timber.d("Network unavailable")
        _isConnected.value = false
      }
    }

    try {
      connectivityManager.registerNetworkCallback(networkRequest, networkCallback!!)
    } catch (e: Exception) {
      Timber.e(e, "Error registering network callback")
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
        trySend(checkCurrentConnectivity())
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

    val request = NetworkRequest.Builder()
      .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
      .build()

    trySend(checkCurrentConnectivity())

    try {
      connectivityManager.registerNetworkCallback(request, callback)
    } catch (e: Exception) {
      Timber.e(e, "Error registering network callback for flow")
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
