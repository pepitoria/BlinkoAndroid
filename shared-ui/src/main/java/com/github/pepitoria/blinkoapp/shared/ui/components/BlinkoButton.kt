package com.github.pepitoria.blinkoapp.shared.ui.components

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.pepitoria.blinkoapp.shared.theme.BlinkoAccent
import com.github.pepitoria.blinkoapp.shared.theme.BlinkoAppTheme

/**
 * Button style variants for BlinkoButton
 */
enum class BlinkoButtonStyle {
  /** Filled button with accent/primary color - use for primary actions */
  Primary,

  /** Outlined button with border - use for secondary actions */
  Secondary,

  /** Text-only button - use for tertiary/cancel actions */
  Text,
}

/**
 * A modern, polished button component for the Blinko app.
 *
 * @param text The button label text
 * @param modifier Modifier for the button
 * @param onClick Callback when the button is clicked
 * @param style The visual style of the button (Primary, Secondary, or Text)
 * @param leadingIcon Optional icon to display before the text
 * @param enabled Whether the button is enabled
 * @param accentColor Custom accent color for primary style buttons (defaults to BlinkoAccent)
 */
@Composable
fun BlinkoButton(
  text: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit = {},
  style: BlinkoButtonStyle = BlinkoButtonStyle.Secondary,
  leadingIcon: ImageVector? = null,
  enabled: Boolean = true,
  accentColor: Color = BlinkoAccent,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  val scale = if (isPressed) 0.96f else 1f
  val shape = RoundedCornerShape(12.dp)

  val contentPadding = PaddingValues(
    horizontal = 20.dp,
    vertical = 12.dp,
  )

  when (style) {
    BlinkoButtonStyle.Primary -> {
      val containerColor by animateColorAsState(
        targetValue = if (isPressed) accentColor.copy(alpha = 0.85f) else accentColor,
        animationSpec = tween(durationMillis = 100),
        label = "containerColor",
      )

      Button(
        onClick = onClick,
        modifier = modifier.scale(scale),
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
          containerColor = containerColor,
          contentColor = Color.White,
          disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
          disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        ),
        elevation = ButtonDefaults.buttonElevation(
          defaultElevation = 2.dp,
          pressedElevation = 0.dp,
          disabledElevation = 0.dp,
        ),
        contentPadding = contentPadding,
        interactionSource = interactionSource,
      ) {
        ButtonContent(
          text = text,
          leadingIcon = leadingIcon,
          contentColor = if (enabled) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        )
      }
    }

    BlinkoButtonStyle.Secondary -> {
      val borderColor by animateColorAsState(
        targetValue = if (isPressed) {
          MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        } else {
          MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        },
        animationSpec = tween(durationMillis = 100),
        label = "borderColor",
      )

      OutlinedButton(
        onClick = onClick,
        modifier = modifier.scale(scale),
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.outlinedButtonColors(
          contentColor = MaterialTheme.colorScheme.onSurface,
          disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        ),
        border = BorderStroke(
          width = 1.5.dp,
          color = if (enabled) borderColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        ),
        contentPadding = contentPadding,
        interactionSource = interactionSource,
      ) {
        ButtonContent(
          text = text,
          leadingIcon = leadingIcon,
          contentColor = if (enabled) {
            MaterialTheme.colorScheme.onSurface
          } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
          },
        )
      }
    }

    BlinkoButtonStyle.Text -> {
      TextButton(
        onClick = onClick,
        modifier = modifier.scale(scale),
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.textButtonColors(
          contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
          disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        ),
        contentPadding = contentPadding,
        interactionSource = interactionSource,
      ) {
        ButtonContent(
          text = text,
          leadingIcon = leadingIcon,
          contentColor = if (enabled) {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
          } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
          },
        )
      }
    }
  }
}

@Composable
private fun ButtonContent(
  text: String,
  leadingIcon: ImageVector?,
  contentColor: Color,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
  ) {
    if (leadingIcon != null) {
      Icon(
        imageVector = leadingIcon,
        contentDescription = null,
        modifier = Modifier.size(18.dp),
        tint = contentColor,
      )
      Spacer(modifier = Modifier.width(8.dp))
    }
    Text(
      text = text,
      fontSize = 15.sp,
      fontWeight = FontWeight.Medium,
      letterSpacing = 0.25.sp,
    )
  }
}

@Preview(showBackground = true, name = "Primary Button")
@Composable
private fun PrimaryButtonPreview() {
  BlinkoAppTheme {
    BlinkoButton(
      text = "Save",
      style = BlinkoButtonStyle.Primary,
      leadingIcon = Icons.Filled.Check,
    )
  }
}

@Preview(showBackground = true, name = "Secondary Button")
@Composable
private fun SecondaryButtonPreview() {
  BlinkoAppTheme {
    BlinkoButton(
      text = "Cancel",
      style = BlinkoButtonStyle.Secondary,
    )
  }
}

@Preview(showBackground = true, name = "Text Button")
@Composable
private fun TextButtonPreview() {
  BlinkoAppTheme {
    BlinkoButton(
      text = "Skip",
      style = BlinkoButtonStyle.Text,
    )
  }
}

@Preview(
  showBackground = true,
  name = "All Buttons - Light",
  uiMode = Configuration.UI_MODE_NIGHT_NO,
  widthDp = 360,
)
@Preview(
  showBackground = true,
  name = "All Buttons - Dark",
  uiMode = Configuration.UI_MODE_NIGHT_YES,
  widthDp = 360,
)
@Composable
private fun BlinkoButtonPreview() {
  BlinkoAppTheme {
    Column(
      modifier = Modifier
        .background(color = MaterialTheme.colorScheme.background)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      // Primary buttons
      Text("Primary Style", color = MaterialTheme.colorScheme.onBackground)
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BlinkoButton(
          text = "Save",
          style = BlinkoButtonStyle.Primary,
          leadingIcon = Icons.Filled.Check,
        )
        BlinkoButton(
          text = "Done",
          style = BlinkoButtonStyle.Primary,
          accentColor = Color(0xFF10B981),
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Secondary buttons
      Text("Secondary Style", color = MaterialTheme.colorScheme.onBackground)
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BlinkoButton(
          text = "Archive",
          style = BlinkoButtonStyle.Secondary,
          leadingIcon = Icons.Filled.Delete,
        )
        BlinkoButton(
          text = "Cancel",
          style = BlinkoButtonStyle.Secondary,
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Text buttons
      Text("Text Style", color = MaterialTheme.colorScheme.onBackground)
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BlinkoButton(
          text = "Skip",
          style = BlinkoButtonStyle.Text,
        )
        BlinkoButton(
          text = "Cancel",
          style = BlinkoButtonStyle.Text,
          leadingIcon = Icons.Filled.Close,
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Disabled states
      Text("Disabled States", color = MaterialTheme.colorScheme.onBackground)
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BlinkoButton(
          text = "Disabled",
          style = BlinkoButtonStyle.Primary,
          enabled = false,
        )
        BlinkoButton(
          text = "Disabled",
          style = BlinkoButtonStyle.Secondary,
          enabled = false,
        )
      }
    }
  }
}
