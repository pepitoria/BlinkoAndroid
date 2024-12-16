package com.github.pepitoria.blinkoapp.ui.sharewithblinko.edit

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.github.pepitoria.blinkoapp.domain.model.note.BlinkoNote
import com.github.pepitoria.blinkoapp.ui.theme.BlinkoAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class ShareAndEditWithBlinkoActivity : ComponentActivity() {

  private val viewModel: ShareAndEditWithBlinkoViewModel by viewModels<ShareAndEditWithBlinkoViewModel>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {

      val uiState = viewModel.noteUiModel.collectAsState()

      BlinkoAppTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          BlinkoNoteEditor(
            uiState = uiState.value,
            modifier = Modifier.padding(innerPadding)
          )
        }
      }
    }
    // Handle the intent
    handleIntent(intent)
  }

  @Composable
  fun BlinkoNoteEditor(
    uiState: BlinkoNote,
    modifier: Modifier = Modifier
  ) {
    Text(uiState.content)
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleIntent(intent)
  }

  private fun handleIntent(intent: Intent) {

    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.noteCreated.collect { noteCreated ->
          if (noteCreated == true) {
            Toast.makeText(this@ShareAndEditWithBlinkoActivity, "Note created", Toast.LENGTH_SHORT).show()
            finish()
          }
        }
      }
    }

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
      viewModel.updateLocalNote(sharedText)
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