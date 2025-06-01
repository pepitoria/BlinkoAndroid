package com.github.pepitoria.blinkoapp

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class BlinkoNavigationRouter(val route: String) {

  data object NavDebug: BlinkoNavigationRouter("debug") {
    data object Debug: BlinkoNavigationRouter("debug/home")
  }

  data object NavAuth: BlinkoNavigationRouter("auth") {
    data object Login: BlinkoNavigationRouter("auth/login")
  }

  data object NavHome: BlinkoNavigationRouter("home") {
    const val ARG_NOTE_ID = "noteId"

    data object NoteList: BlinkoNavigationRouter("home/note-list")
    data object BlinkoList: BlinkoNavigationRouter("home/blinko-list")
    data object TodoList: BlinkoNavigationRouter("home/todo-list")
    data object Search: BlinkoNavigationRouter("home/search")
    data object NoteEdit: BlinkoNavigationRouter("home/note-edit/{$ARG_NOTE_ID}") {
      val arguments : List<NamedNavArgument> = listOf(navArgument(ARG_NOTE_ID) { type = NavType.IntType })
      fun createRoute(noteId: Int) = "home/note-edit/$noteId"
    }
    data object Settings: BlinkoNavigationRouter("home/settings")

  }
}