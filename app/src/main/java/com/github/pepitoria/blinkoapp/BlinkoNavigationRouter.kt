package com.github.pepitoria.blinkoapp

sealed class BlinkoNavigationRouter(val route: String) {

  data object NavDebug: BlinkoNavigationRouter("debug") {
    data object Debug: BlinkoNavigationRouter("debug/home")
  }

  data object NavAuth: BlinkoNavigationRouter("auth") {
    data object Login: BlinkoNavigationRouter("auth/login")
  }

  data object NavHome: BlinkoNavigationRouter("home") {
    data object NoteList: BlinkoNavigationRouter("home/note-list")
    data object NoteEdit: BlinkoNavigationRouter("home/note-edit")
  }
}