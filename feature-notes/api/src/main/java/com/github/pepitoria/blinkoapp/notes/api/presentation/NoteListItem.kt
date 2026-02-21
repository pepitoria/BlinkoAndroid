package com.github.pepitoria.blinkoapp.notes.api.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.pepitoria.blinkoapp.notes.api.R
import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNote
import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNoteType
import com.github.pepitoria.blinkoapp.shared.ui.sync.NoteSyncStatusIcon
import com.halilibo.richtext.commonmark.Markdown
import com.halilibo.richtext.ui.BasicRichText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListItem(
  note: BlinkoNote,
  onClick: (Int) -> Unit = { _ -> },
  onDeleteSwipe: (BlinkoNote) -> Unit = { _ -> },
  markAsDone: (BlinkoNote) -> Unit = { _ -> },
  onConflictClick: ((BlinkoNote) -> Unit)? = null,
) {
  val dismissState = rememberSwipeToDismissBoxState(
    positionalThreshold = { totalDistance ->
      totalDistance * 0.5f
    },
    confirmValueChange = { newValue ->
      when (newValue) {
        SwipeToDismissBoxValue.EndToStart -> {
          onDeleteSwipe(note)
          false
        }
        else -> {
          false
        }
      }
    },
  )

  val modifier = if (note.isArchived) {
    Modifier.graphicsLayer(alpha = 0.7f)
  } else {
    Modifier
  }

  SwipeToDismissBox(
    modifier = modifier,
    state = dismissState,
    backgroundContent = {
      Box(
        modifier = Modifier
          .fillMaxSize(),
        contentAlignment = Alignment.CenterEnd,
      ) {
        Text(
          text = stringResource(R.string.notes_release_to_delete),
          modifier = Modifier
            .padding(4.dp)
            .align(Alignment.Center),
        )
        Icon(
          imageVector = Icons.Filled.Delete,
          contentDescription = stringResource(id = R.string.notes_delete_note),
          tint = MaterialTheme.colorScheme.tertiary,
        )
      }
    },
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth(),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.background,
      ),
      elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
      onClick = {
        if (note.hasConflict && onConflictClick != null) {
          onConflictClick(note)
        } else {
          note.id?.let(onClick)
        }
      },
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        var isChecked by remember { mutableStateOf(note.isArchived) }

        if (note.type == BlinkoNoteType.TODO) {
          Checkbox(
            checked = isChecked,
            onCheckedChange = {
              isChecked = it
              markAsDone(note.copy(isArchived = isChecked))
            },
            modifier = Modifier.padding(
              top = 4.dp,
              start = 4.dp,
              bottom = 4.dp,
              end = 0.dp,
            ),
          )
        }

        BasicRichText(
          modifier = Modifier
            .weight(1f)
            .padding(
              top = 16.dp,
              start = if (note.type == BlinkoNoteType.TODO) 4.dp else 16.dp,
              bottom = 16.dp,
              end = 8.dp,
            ),
        ) {
          Markdown(
            content = note.content.trimIndent(),
          )
        }

        // Sync status indicator
        if (note.isPending || note.hasConflict) {
          Box(
            modifier = Modifier
              .padding(end = 12.dp)
              .then(
                if (note.hasConflict && onConflictClick != null) {
                  Modifier.clickable { onConflictClick(note) }
                } else {
                  Modifier
                },
              ),
          ) {
            NoteSyncStatusIcon(
              isPending = note.isPending,
              hasConflict = note.hasConflict,
            )
          }
        }
      }
    }
  }
}
