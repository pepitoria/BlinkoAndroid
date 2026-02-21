package com.github.pepitoria.blinkoapp.notes.implementation.presentation.conflict

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNote
import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNoteType
import com.github.pepitoria.blinkoapp.notes.api.domain.model.SyncStatus
import com.github.pepitoria.blinkoapp.shared.theme.BlinkoAppTheme
import com.github.pepitoria.blinkoapp.shared.ui.R

@Composable
fun ConflictResolutionDialog(
  note: BlinkoNote,
  onKeepLocal: () -> Unit,
  onKeepServer: () -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    icon = {
      Icon(
        imageVector = Icons.Default.Warning,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.error,
      )
    },
    title = {
      Text(text = stringResource(id = R.string.sync_conflict_title))
    },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
      ) {
        Text(
          text = stringResource(id = R.string.sync_conflict_description),
          style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Local version preview
        ConflictVersionCard(
          title = stringResource(id = R.string.sync_conflict_local_version),
          icon = {
            Icon(
              imageVector = Icons.Default.PhoneAndroid,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
            )
          },
          content = note.content,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Server version placeholder
        ConflictVersionCard(
          title = stringResource(id = R.string.sync_conflict_server_version),
          icon = {
            Icon(
              imageVector = Icons.Default.Cloud,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.secondary,
            )
          },
          content = stringResource(id = R.string.sync_conflict_server_version_hint),
          isHint = true,
        )
      }
    },
    confirmButton = {
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        OutlinedButton(onClick = onKeepServer) {
          Icon(
            imageVector = Icons.Default.Cloud,
            contentDescription = null,
            modifier = Modifier.padding(end = 4.dp),
          )
          Text(text = stringResource(id = R.string.sync_conflict_keep_server))
        }
        TextButton(onClick = onKeepLocal) {
          Icon(
            imageVector = Icons.Default.PhoneAndroid,
            contentDescription = null,
            modifier = Modifier.padding(end = 4.dp),
          )
          Text(text = stringResource(id = R.string.sync_conflict_keep_local))
        }
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(text = stringResource(id = R.string.sync_conflict_later))
      }
    },
  )
}

@Composable
private fun ConflictVersionCard(
  title: String,
  icon: @Composable () -> Unit,
  content: String,
  isHint: Boolean = false,
) {
  Card(
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    ),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
      ) {
        icon()
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = title,
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = content,
        style = if (isHint) {
          MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
          )
        } else {
          MaterialTheme.typography.bodySmall
        },
        maxLines = 5,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Preview
@Composable
private fun ConflictResolutionDialogPreview() {
  BlinkoAppTheme {
    ConflictResolutionDialog(
      note = BlinkoNote(
        id = 1,
        localId = "local-123",
        content = "This is my local version of the note with some changes I made while offline.",
        type = BlinkoNoteType.NOTE,
        isArchived = false,
        syncStatus = SyncStatus.CONFLICT,
      ),
      onKeepLocal = {},
      onKeepServer = {},
      onDismiss = {},
    )
  }
}
