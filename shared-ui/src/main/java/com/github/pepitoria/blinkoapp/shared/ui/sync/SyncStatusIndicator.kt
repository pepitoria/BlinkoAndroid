package com.github.pepitoria.blinkoapp.shared.ui.sync

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ConnectionStatusIndicator(
  isConnected: Boolean,
  pendingSyncCount: Int,
  modifier: Modifier = Modifier,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .background(
        color = if (isConnected) {
          MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        } else {
          MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
        },
        shape = RoundedCornerShape(16.dp),
      )
      .padding(horizontal = 12.dp, vertical = 6.dp),
  ) {
    if (isConnected) {
      if (pendingSyncCount > 0) {
        SyncingIcon()
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Syncing ($pendingSyncCount)",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      } else {
        Icon(
          imageVector = Icons.Default.Cloud,
          contentDescription = "Online",
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Online",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    } else {
      Icon(
        imageVector = Icons.Default.CloudOff,
        contentDescription = "Offline",
        tint = MaterialTheme.colorScheme.error,
        modifier = Modifier.size(18.dp),
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = if (pendingSyncCount > 0) "Offline ($pendingSyncCount pending)" else "Offline",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onErrorContainer,
      )
    }
  }
}

@Composable
private fun SyncingIcon() {
  val infiniteTransition = rememberInfiniteTransition(label = "sync_rotation")
  val rotation by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1500, easing = LinearEasing),
      repeatMode = RepeatMode.Restart,
    ),
    label = "rotation",
  )

  Icon(
    imageVector = Icons.Default.CloudSync,
    contentDescription = "Syncing",
    tint = MaterialTheme.colorScheme.primary,
    modifier = Modifier
      .size(18.dp)
      .rotate(rotation),
  )
}

@Composable
fun NoteSyncStatusIcon(
  isPending: Boolean,
  hasConflict: Boolean,
  modifier: Modifier = Modifier,
) {
  when {
    hasConflict -> {
      Icon(
        imageVector = Icons.Default.Warning,
        contentDescription = "Sync conflict",
        tint = MaterialTheme.colorScheme.error,
        modifier = modifier.size(16.dp),
      )
    }
    isPending -> {
      val infiniteTransition = rememberInfiniteTransition(label = "pending_pulse")
      val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
          animation = tween(durationMillis = 1000, easing = LinearEasing),
          repeatMode = RepeatMode.Reverse,
        ),
        label = "alpha",
      )

      Icon(
        imageVector = Icons.Default.CloudSync,
        contentDescription = "Pending sync",
        tint = MaterialTheme.colorScheme.tertiary.copy(alpha = alpha),
        modifier = modifier.size(16.dp),
      )
    }
  }
}

@Composable
fun PendingSyncBadge(
  count: Int,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  if (count > 0) {
    BadgedBox(
      badge = {
        Badge(
          containerColor = MaterialTheme.colorScheme.tertiary,
          contentColor = MaterialTheme.colorScheme.onTertiary,
        ) {
          Text(
            text = if (count > 99) "99+" else count.toString(),
            fontSize = 10.sp,
          )
        }
      },
      modifier = modifier,
    ) {
      content()
    }
  } else {
    Box(modifier = modifier) {
      content()
    }
  }
}

@Composable
fun OfflineBanner(
  pendingSyncCount: Int,
  modifier: Modifier = Modifier,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .background(MaterialTheme.colorScheme.errorContainer)
      .padding(horizontal = 16.dp, vertical = 8.dp),
  ) {
    Icon(
      imageVector = Icons.Default.CloudOff,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onErrorContainer,
      modifier = Modifier.size(20.dp),
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text = if (pendingSyncCount > 0) {
        "You're offline. $pendingSyncCount changes pending sync."
      } else {
        "You're offline. Changes will sync when back online."
      },
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onErrorContainer,
    )
  }
}

@Composable
fun ConflictIndicator(
  conflictCount: Int,
  modifier: Modifier = Modifier,
) {
  if (conflictCount > 0) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = modifier
        .background(
          color = MaterialTheme.colorScheme.errorContainer,
          shape = RoundedCornerShape(16.dp),
        )
        .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
      Icon(
        imageVector = Icons.Default.Warning,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.error,
        modifier = Modifier.size(18.dp),
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = "$conflictCount conflict${if (conflictCount > 1) "s" else ""} to resolve",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onErrorContainer,
      )
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun ConnectionStatusIndicatorOnlinePreview() {
  ConnectionStatusIndicator(isConnected = true, pendingSyncCount = 0)
}

@Preview(showBackground = true)
@Composable
private fun ConnectionStatusIndicatorSyncingPreview() {
  ConnectionStatusIndicator(isConnected = true, pendingSyncCount = 3)
}

@Preview(showBackground = true)
@Composable
private fun ConnectionStatusIndicatorOfflinePreview() {
  ConnectionStatusIndicator(isConnected = false, pendingSyncCount = 5)
}

@Preview(showBackground = true)
@Composable
private fun OfflineBannerPreview() {
  OfflineBanner(pendingSyncCount = 3)
}

@Preview(showBackground = true)
@Composable
private fun ConflictIndicatorPreview() {
  ConflictIndicator(conflictCount = 2)
}
