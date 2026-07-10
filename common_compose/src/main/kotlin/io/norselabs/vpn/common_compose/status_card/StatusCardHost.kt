package io.norselabs.vpn.common_compose.status_card

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.norselabs.vpn.common.status_card.StatusCardController
import io.norselabs.vpn.common.status_card.StatusCardState

@Composable
fun BoxScope.StatusCardHost(
  controller: StatusCardController,
  modifier: Modifier = Modifier,
  alignment: Alignment = Alignment.BottomCenter,
  contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
  enter: EnterTransition = slideInVertically { it },
  exit: ExitTransition = slideOutVertically { it },
  colors: StatusCardColors = StatusCardDefaults.colors(),
  icons: StatusCardIcons = StatusCardDefaults.icons(),
) {
  val state by controller.state.collectAsState()
  StatusCardHost(
    state = state,
    onRetry = controller::onRetryClick,
    modifier = modifier,
    alignment = alignment,
    contentPadding = contentPadding,
    enter = enter,
    exit = exit,
    colors = colors,
    icons = icons,
  )
}

@Composable
fun BoxScope.StatusCardHost(
  state: StatusCardState?,
  onRetry: () -> Unit,
  modifier: Modifier = Modifier,
  alignment: Alignment = Alignment.BottomCenter,
  contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
  enter: EnterTransition = slideInVertically { it },
  exit: ExitTransition = slideOutVertically { it },
  colors: StatusCardColors = StatusCardDefaults.colors(),
  icons: StatusCardIcons = StatusCardDefaults.icons(),
) {
  // Keeps the last non-null state so the card content stays on screen
  // while the exit animation is running.
  var lastVisibleState by remember { mutableStateOf(state) }
  if (state != null) {
    lastVisibleState = state
  }
  AnimatedVisibility(
    visible = state != null,
    enter = enter,
    exit = exit,
    modifier = modifier
      .align(alignment)
      .fillMaxWidth()
      .padding(contentPadding),
  ) {
    lastVisibleState?.let {
      StatusCard(
        state = it,
        onRetry = onRetry,
        colors = colors,
        icons = icons,
      )
    }
  }
}
