package io.norselabs.vpn.common_compose.prompt_sheet

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Renders the prompt requested through [state] as a Material3 [AlertDialog] —
 * the alert-shaped sibling of [PromptSheetHost] over the same [PromptHostState]
 * contract, so the caller of `show()` doesn't care which form is on screen.
 * Button roles and theming reuse [PromptSheetColors]/[PromptSheetTextStyles]
 * (the roles are identical). `onDismissRequest` (scrim tap / back press) maps
 * to [PromptResult.Closed].
 */
@Composable
fun PromptDialogHost(
  state: PromptHostState,
  containerColor: Color = MaterialTheme.colorScheme.surface,
  colors: PromptSheetColors = PromptSheetDefaults.colors(),
  textStyles: PromptSheetTextStyles = PromptSheetDefaults.textStyles(),
) {
  val request = state.current ?: return

  AlertDialog(
    onDismissRequest = { request.resolve(PromptResult.Closed) },
    containerColor = containerColor,
    title = {
      Text(
        text = request.data.title,
        style = textStyles.title,
        color = colors.title,
      )
    },
    text = request.data.description?.let { description ->
      {
        Text(
          text = description,
          style = textStyles.description,
          color = colors.description,
        )
      }
    },
    confirmButton = {
      PromptButton(
        text = request.data.confirmLabel,
        onClick = { request.resolve(PromptResult.Confirmed) },
        containerColor = colors.confirmContainer,
        contentColor = colors.confirmContent,
        textStyle = textStyles.button,
      )
    },
    dismissButton = request.data.dismissLabel?.let { label ->
      {
        PromptButton(
          text = label,
          onClick = { request.resolve(PromptResult.Dismissed) },
          containerColor = colors.dismissContainer,
          contentColor = colors.dismissContent,
          textStyle = textStyles.button,
        )
      }
    },
  )
}
