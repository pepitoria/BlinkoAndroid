package com.github.pepitoria.blinkoapp.ui.sharewithblinko

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.github.pepitoria.blinkoapp.ui.theme.BlinkoAppTheme
import timber.log.Timber

class ShareWithBlinkoActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      BlinkoAppTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          Greeting(
            name = "Android",
            modifier = Modifier.padding(innerPadding)
          )
        }
      }
    }
    // Handle the intent
    handleIntent(intent)
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleIntent(intent)
  }

  private fun handleIntent(intent: Intent) {
    when (intent.action) {
      Intent.ACTION_SEND -> {
        if (intent.type?.startsWith("text/") == true) {
          handleText(intent)
        } else if (intent.type?.startsWith("image/") == true) {
          handleImage(intent)
        }
      }
      Intent.ACTION_SEND_MULTIPLE -> {
        // Handle multiple items being shared
      }
    }
  }

  private fun handleText(intent: Intent) {
    intent.getStringExtra(Intent.EXTRA_TEXT)?.let { sharedText ->
      // Process the shared text
      // (e.g., display it in a TextView, save it to a database)
      Timber.d("handleText: $sharedText")
    }
  }

  private fun handleImage(intent: Intent) {
    Timber.d("handleImage")
    intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let { imageUri ->
      // Handle the image URI
    }
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(
    text = "Hello $name!",
    modifier = modifier
  )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  BlinkoAppTheme {
    Greeting("Android")
  }
}