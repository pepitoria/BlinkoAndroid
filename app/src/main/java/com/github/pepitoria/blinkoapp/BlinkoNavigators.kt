package com.github.pepitoria.blinkoapp

import android.content.Context
import android.content.Intent
import androidx.navigation.NavHostController
import com.github.pepitoria.blinkoapp.ui.sharewithblinko.edit.ShareAndEditWithBlinkoActivity

fun NavHostController.goToEditWithBlinko(): () -> Unit = {
  val intent = Intent(context, ShareAndEditWithBlinkoActivity::class.java)
  intent.action = Intent.ACTION_SEND
  intent.type = "text/plain"
  intent.putExtra(Intent.EXTRA_TEXT, "Hello, Blinko lalala!")
  context.startActivity(intent)
}

fun NavHostController.goToDebug(): () -> Unit = {
  this.navigate(route = BlinkoNavigationRouter.NavDebug.Debug.route)
}

fun NavHostController.goToNoteList(): () -> Unit = {
  this.navigate(route = BlinkoNavigationRouter.NavHome.NoteList.route)
}

fun NavHostController.goToNoteEdit(): (id: Int) -> Unit = { id ->
  this.navigate(route = BlinkoNavigationRouter.NavHome.NoteEdit.createRoute(noteId = id))
}