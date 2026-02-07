package com.github.pepitoria.blinkoapp.shared.navigation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.github.pepitoria.blinkoapp.auth.api.AuthFactory
import com.github.pepitoria.blinkoapp.notes.api.NotesFactory
import com.github.pepitoria.blinkoapp.search.api.SearchFactory
import com.github.pepitoria.blinkoapp.settings.api.SettingsFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NavigationActivity : ComponentActivity() {

  @Inject
  lateinit var searchFactory: SearchFactory

  @Inject
  lateinit var authFactory: AuthFactory

  @Inject
  lateinit var settingsFactory: SettingsFactory

  @Inject
  lateinit var notesFactory: NotesFactory

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      val navController = rememberNavController()
      BlinkoNavigationController(navController, searchFactory, authFactory, settingsFactory, notesFactory)
    }
  }
}
