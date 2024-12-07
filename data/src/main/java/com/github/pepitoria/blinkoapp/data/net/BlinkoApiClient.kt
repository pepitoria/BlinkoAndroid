package com.github.pepitoria.blinkoapp.data.net

import android.content.Context
import android.net.ConnectivityManager
import com.github.pepitoria.blinkoapp.data.model.login.LoginRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class BlinkoApiClient @Inject constructor(
  @ApplicationContext private val appContext: Context,
  private val blinkoApi: BlinkoApi,
) {

  fun login(loginRequest: LoginRequest) {
    if (!isConnected()) {
      //TODO handle no internet connection
      return
    }


  }
  fun isConnected(): Boolean {
    val connectivityManager =
      appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val netInfo = connectivityManager.activeNetworkInfo
    return netInfo != null && netInfo.isConnected
  }

}