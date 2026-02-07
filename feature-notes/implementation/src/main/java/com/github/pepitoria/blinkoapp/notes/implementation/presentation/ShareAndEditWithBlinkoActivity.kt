package com.github.pepitoria.blinkoapp.notes.implementation.presentation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNote
import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNoteType
import com.github.pepitoria.blinkoapp.shared.theme.BlinkoAppTheme
import com.github.pepitoria.blinkoapp.shared.ui.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class ShareAndEditWithBlinkoActivity : ComponentActivity() {

  companion object {
    const val NOTE_TYPE = "ShareAndEditWithBlinkoActivity.NOTE_TYPE"
  }

  private val viewModel: NoteEditScreenViewModel by viewModels<NoteEditScreenViewModel>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val noteType = intent.getIntExtra(NOTE_TYPE, BlinkoNoteType.BLINKO.value)
    setContent {
      val uiState = viewModel.noteUiModel.collectAsState()
      val noteTypes = viewModel.noteTypes.collectAsState()

      BlinkoAppTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          BlinkoNoteEditor(
            uiState = uiState.value,
            noteTypes = noteTypes.value,
            defaultNoteType = BlinkoNoteType.fromResponseType(noteType),
            modifier = Modifier.padding(innerPadding),
            updateNote = {
              viewModel.updateLocalNote(
                content = it.content,
                noteType = it.type.value,
              )
            },
            sendToBlinko = { viewModel.upsertNote() },
            goBack = { finish() },
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
    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.onStart(onNoteUpsert = {
          Toast.makeText(
            this@ShareAndEditWithBlinkoActivity,
            getString(R.string.note_created),
            Toast.LENGTH_SHORT,
          ).show()
          finish()
        })
      }
    }

    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.error.collect { error ->
          error?.let {
            Toast.makeText(
              this@ShareAndEditWithBlinkoActivity,
              getString(R.string.error_toast, error),
              Toast.LENGTH_SHORT,
            ).show()
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
      viewModel.updateLocalNote(content = sharedText)
    }

    intent.getIntExtra(NOTE_TYPE, BlinkoNoteType.BLINKO.value).let { noteType ->
      viewModel.updateLocalNote(noteType = noteType)
    }
  }

  private fun handleImage(intent: Intent) {
    Timber.d("handleImage")
    intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let { imageUri ->
      // Handle the image URI
    }
  }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  BlinkoAppTheme {
    BlinkoNoteEditor(
      BlinkoNote(
        content = "Hello, Blinko!",
        type = BlinkoNoteType.BLINKO,
        isArchived = false,
      ),
      noteTypes = listOf(
        BlinkoNoteType.BLINKO.value,
        BlinkoNoteType.NOTE.value,
        BlinkoNoteType.TODO.value,
      ),
      defaultNoteType = BlinkoNoteType.BLINKO,
      updateNote = {},
      sendToBlinko = {},
      goBack = {},
    )
  }
}
