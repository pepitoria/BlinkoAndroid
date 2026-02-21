package com.github.pepitoria.blinkoapp.shared.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
  primary = Black,
  secondary = White,
  tertiary = GreyBackground,
  background = RDGreyBackgroundDarker,
  surface = RDGreyBackgroundDark,
  onBackground = White,

  onPrimary = White,
  onSecondary = Black,
  onSurface = White,
)

private val LightColorScheme = lightColorScheme(
  primary = White,
  secondary = Black,
  tertiary = GreyBackground,
  background = RDGreyBackgroundLight,
  surface = White,
  onBackground = Black,

  onPrimary = Black,
  onSecondary = White,
  onSurface = Black,
)

object BlinkoAppTheme {

  @Composable
  fun cardColors(): CardColors = CardDefaults.cardColors(
    containerColor = MaterialTheme.colorScheme.background,
  )
}

@Composable
fun BlinkoAppTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme = when {
//    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//      val context = LocalContext.current
//      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as Activity).window
      window.statusBarColor = colorScheme.primary.toArgb()
      WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content,
  )
}

@Composable
fun getBackgroundColor(): Color {
  return MaterialTheme.colorScheme.background
}

/**
 * Returns the accent color for a note type.
 * @param noteTypeValue 0 = Blinko (gold), 1 = Note (blue), 2 = Todo (green)
 */
fun getNoteTypeAccentColor(noteTypeValue: Int): Color {
  return when (noteTypeValue) {
    0 -> BlinkoAccent
    1 -> NoteAccent
    2 -> TodoAccent
    else -> BlinkoAccent
  }
}
