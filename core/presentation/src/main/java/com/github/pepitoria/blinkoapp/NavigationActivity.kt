package com.github.pepitoria.blinkoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.github.pepitoria.blinkoapp.auth.api.AuthFactory
import com.github.pepitoria.blinkoapp.search.api.SearchFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NavigationActivity : ComponentActivity() {

  @Inject
  lateinit var searchFactory: SearchFactory

  @Inject
  lateinit var authFactory: AuthFactory

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      val navController = rememberNavController()
      BlinkoNavigationController(navController, searchFactory, authFactory)
    }
  }

}