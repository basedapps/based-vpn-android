package io.norselabs.vpn.common_compose.prompt_sheet

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Renders the prompt requested through [state] as a Material3 [ModalBottomSheet]
 * with [PromptSheetContent] inside. Place it once per screen, anywhere in the
 * hierarchy (the sheet is drawn in its own window). M3's `onDismissRequest`
 * covers swipe/scrim/back natively — the reason this host exists instead of a
 * Voyager BottomSheetNavigator screen, which reports none of those.
 * Insets are disabled on the sheet itself: [PromptSheetContent] already ends
 * with a navigation-bar spacer.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptSheetHost(
  state: PromptHostState,
  modifier: Modifier = Modifier,
  shape: Shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
  containerColor: Color = MaterialTheme.colorScheme.surface,
  dragHandle: (@Composable () -> Unit)? = null,
  colors: PromptSheetColors = PromptSheetDefaults.colors(),
  textStyles: PromptSheetTextStyles = PromptSheetDefaults.textStyles(),
) {
  val request = state.current ?: return
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val scope = rememberCoroutineScope()

  fun hideThenResolve(result: PromptResult) {
    scope.launch {
      sheetState.hide()
      request.resolve(result)
    }
  }

  ModalBottomSheet(
    onDismissRequest = { request.resolve(PromptResult.Closed) },
    sheetState = sheetState,
    shape = shape,
    containerColor = containerColor,
    dragHandle = dragHandle,
    contentWindowInsets = { WindowInsets(0) },
    modifier = modifier,
  ) {
    PromptSheetContent(
      title = request.data.title,
      description = request.data.description,
      confirmLabel = request.data.confirmLabel,
      onConfirmClick = { hideThenResolve(PromptResult.Confirmed) },
      dismissLabel = request.data.dismissLabel,
      onDismissClick = { hideThenResolve(PromptResult.Dismissed) },
      colors = colors,
      textStyles = textStyles,
    )
  }
}
