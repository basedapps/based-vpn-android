package io.norselabs.vpn.common_compose.prompt_sheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.norselabs.vpn.common_compose.HorizontalSpacer
import io.norselabs.vpn.common_compose.NavigationSpacer
import io.norselabs.vpn.common_compose.VerticalSpacer

/**
 * Content of a confirmation bottom sheet: title, description and one or two buttons.
 * Colors and text styles default to [MaterialTheme], so a branded color scheme skins
 * the sheet with no arguments at the call site (same approach as StatusCard).
 * The hosting bottom-sheet Screen (navigation, view-model wiring) stays in the app.
 */
@Composable
fun PromptSheetContent(
  title: String,
  description: String,
  confirmLabel: String,
  onConfirmClick: () -> Unit,
  modifier: Modifier = Modifier,
  dismissLabel: String? = null,
  onDismissClick: (() -> Unit)? = null,
  colors: PromptSheetColors = PromptSheetDefaults.colors(),
  textStyles: PromptSheetTextStyles = PromptSheetDefaults.textStyles(),
) {
  Column(
    modifier = modifier.padding(horizontal = 16.dp),
  ) {
    VerticalSpacer(24.dp)
    Text(
      text = title,
      style = textStyles.title,
      color = colors.title,
      modifier = Modifier.fillMaxWidth(),
    )
    VerticalSpacer(12.dp)
    Text(
      text = description,
      style = textStyles.description,
      color = colors.description,
    )
    VerticalSpacer(20.dp)
    Row {
      PromptButton(
        text = confirmLabel,
        onClick = onConfirmClick,
        containerColor = colors.confirmContainer,
        contentColor = colors.confirmContent,
        textStyle = textStyles.button,
      )
      if (dismissLabel != null) {
        HorizontalSpacer(16.dp)
        PromptButton(
          text = dismissLabel,
          onClick = onDismissClick ?: {},
          containerColor = colors.dismissContainer,
          contentColor = colors.dismissContent,
          textStyle = textStyles.button,
        )
      }
    }
    VerticalSpacer(16.dp)
    NavigationSpacer()
  }
}

@Composable
private fun PromptButton(
  text: String,
  onClick: () -> Unit,
  containerColor: Color,
  contentColor: Color,
  textStyle: TextStyle,
) {
  Button(
    onClick = onClick,
    colors = ButtonDefaults.buttonColors(
      containerColor = containerColor,
      contentColor = contentColor,
    ),
    shape = RoundedCornerShape(8.dp),
    modifier = Modifier
      .height(40.dp)
      .widthIn(min = 120.dp),
  ) {
    Text(
      text = text,
      style = textStyle,
      textAlign = TextAlign.Center,
    )
  }
}

@Immutable
class PromptSheetColors(
  val title: Color,
  val description: Color,
  val confirmContainer: Color,
  val confirmContent: Color,
  val dismissContainer: Color,
  val dismissContent: Color,
)

@Immutable
class PromptSheetTextStyles(
  val title: TextStyle,
  val description: TextStyle,
  val button: TextStyle,
)

object PromptSheetDefaults {

  @Composable
  fun colors(
    title: Color = MaterialTheme.colorScheme.onSurface,
    description: Color = MaterialTheme.colorScheme.onSurface,
    confirmContainer: Color = MaterialTheme.colorScheme.primary,
    confirmContent: Color = MaterialTheme.colorScheme.onPrimary,
    dismissContainer: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    dismissContent: Color = MaterialTheme.colorScheme.onSurface,
  ): PromptSheetColors = PromptSheetColors(
    title = title,
    description = description,
    confirmContainer = confirmContainer,
    confirmContent = confirmContent,
    dismissContainer = dismissContainer,
    dismissContent = dismissContent,
  )

  @Composable
  fun textStyles(
    title: TextStyle = MaterialTheme.typography.headlineSmall,
    description: TextStyle = MaterialTheme.typography.bodyLarge,
    button: TextStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
  ): PromptSheetTextStyles = PromptSheetTextStyles(
    title = title,
    description = description,
    button = button,
  )
}

@Preview(showBackground = true)
@Composable
private fun PromptSheetContentPreview() {
  MaterialTheme {
    PromptSheetContent(
      title = "Enable notifications",
      description = "Allow notifications to see your VPN connection status.",
      confirmLabel = "Enable",
      onConfirmClick = {},
      dismissLabel = "Not now",
      onDismissClick = {},
    )
  }
}
