package io.norselabs.vpn.common_compose.status_card

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.norselabs.vpn.common.status_card.StatusCardController
import io.norselabs.vpn.common.status_card.StatusCardState

@Composable
fun BoxScope.StatusCardHost(
  controller: StatusCardController,
  modifier: Modifier = Modifier,
  alignment: Alignment = Alignment.BottomCenter,
  contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
  bottomFade: Dp = 12.dp,
  enter: EnterTransition = slideInVertically(
    animationSpec = tween(durationMillis = 250, easing = LinearOutSlowInEasing),
  ) { it * 2 },
  exit: ExitTransition = slideOutVertically(
    animationSpec = tween(durationMillis = 200, easing = FastOutLinearInEasing),
  ) { it * 2 },
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
    bottomFade = bottomFade,
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
  bottomFade: Dp = 12.dp,
  enter: EnterTransition = slideInVertically(
    animationSpec = tween(durationMillis = 250, easing = LinearOutSlowInEasing),
  ) { it * 2 },
  exit: ExitTransition = slideOutVertically(
    animationSpec = tween(durationMillis = 200, easing = FastOutLinearInEasing),
  ) { it * 2 },
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
      .bottomFade(bottomFade)
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

// Clips the sliding card to the host bounds and dissolves it into a
// gradient alpha mask at the bottom edge instead of a hard cut. The mask
// must not be taller than the bottom content padding, otherwise it eats
// into the resting card.
private fun Modifier.bottomFade(height: Dp): Modifier {
  if (height <= 0.dp) return this
  return this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
      drawContent()
      val fadePx = height.toPx().coerceAtMost(size.height)
      drawRect(
        brush = Brush.verticalGradient(
          colors = listOf(Color.Black, Color.Transparent),
          startY = size.height - fadePx,
          endY = size.height,
        ),
        topLeft = Offset(0f, size.height - fadePx),
        size = Size(size.width, fadePx),
        blendMode = BlendMode.DstIn,
      )
    }
}
