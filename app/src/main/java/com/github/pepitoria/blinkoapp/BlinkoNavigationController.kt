package com.github.pepitoria.blinkoapp

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.github.pepitoria.blinkoapp.ui.login.TokenLoginWidget
import com.github.pepitoria.blinkoapp.ui.sharewithblinko.edit.ShareAndEditWithBlinkoActivity

@Composable
fun BlinkoNavigationController(
  navController: NavHostController,
) {
  NavHost(
    navController = navController,
    startDestination = BlinkoNavigationRouter.NavAuth.route,
    modifier = Modifier
      .fillMaxSize(),
  ) {
    ///// AUTHENTICATION
    navigation(
      startDestination = BlinkoNavigationRouter.NavAuth.Login.route,
      route = BlinkoNavigationRouter.NavAuth.route,
    ) {
      composable(BlinkoNavigationRouter.NavAuth.Login.route) {
        LoginNavigator(navController = navController)
      }
    }
  }
}

@Composable
fun LoginNavigator(
  navController: NavHostController,
) {
  TokenLoginWidget(
    goToEditWithBlinko = {
      goToEditWithBlinko(context = navController.context)
    }
  )
}

private fun goToEditWithBlinko(context: Context) {
  val intent = Intent(context, ShareAndEditWithBlinkoActivity::class.java)
  intent.action = Intent.ACTION_SEND
  intent.type = "text/plain"
  intent.putExtra(Intent.EXTRA_TEXT, "Hello, Blinko lalala!")
  context.startActivity(intent)
}