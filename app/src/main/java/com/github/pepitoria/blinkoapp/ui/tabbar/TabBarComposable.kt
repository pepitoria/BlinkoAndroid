package com.github.pepitoria.blinkoapp.ui.tabbar

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.github.pepitoria.blinkoapp.BlinkoNavigationRouter

@Composable
fun TabBar(
  currentRoute: String,
  goToNotes: () -> Unit,
  goToBlinkos: () -> Unit,
  content: @Composable (PaddingValues) -> Unit
) {
  Scaffold(
    bottomBar = {
      NavigationBar {
        NavigationBarItem(
          icon = { Icon(Icons.Default.Home, contentDescription = "Notes") },
          label = { Text("Notes") },
          selected = currentRoute == BlinkoNavigationRouter.NavHome.NoteList.route,
          onClick = {
            if (currentRoute != BlinkoNavigationRouter.NavHome.NoteList.route) {
              goToNotes()
            }
          }
        )

        NavigationBarItem(
          icon = { Icon(Icons.Default.Search, contentDescription = "Blinkos") },
          label = { Text("Blinkos") },
          selected = currentRoute == BlinkoNavigationRouter.NavHome.BlinkoList.route,
          onClick = {
            if (currentRoute != BlinkoNavigationRouter.NavHome.BlinkoList.route) {
              goToBlinkos()
            }
          }
        )
      }
    }
  ) { innerPadding ->
    content(innerPadding)
  }
}