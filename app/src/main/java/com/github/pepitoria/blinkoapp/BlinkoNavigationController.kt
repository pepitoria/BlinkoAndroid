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
import com.github.pepitoria.blinkoapp.ui.debug.DebugScreenComposable
import com.github.pepitoria.blinkoapp.ui.login.TokenLoginWidget

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
    ///// Debug
    navigation(
      startDestination = BlinkoNavigationRouter.NavDebug.Debug.route,
      route = BlinkoNavigationRouter.NavDebug.route,
    ) {
      composable(BlinkoNavigationRouter.NavDebug.Debug.route) {
        DebugNavigator(navController = navController)
      }
    }

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
    goToDebug = navController.goToDebug(),
  )
}

@Composable
fun DebugNavigator(
  navController: NavHostController,
) {
  DebugScreenComposable(
    goToEditWithBlinko = navController.goToEditWithBlinko(),
  )
}