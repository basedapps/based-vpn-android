package io.norselabs.vpn.common_compose.status_card

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.norselabs.vpn.common.status_card.StatusCardState
import io.norselabs.vpn.common_compose.HorizontalSpacer

@Immutable
data class StatusCardColors(
  val container: Color,
  val title: Color,
  val subtitle: Color,
  val loadingIconContainer: Color,
  val loadingIndicator: Color,
  val successIconContainer: Color,
  val successIcon: Color,
  val infoIconContainer: Color,
  val infoIcon: Color,
  val errorIconContainer: Color,
  val errorIcon: Color,
  val retryButtonContainer: Color,
  val retryButtonIcon: Color,
)

@Immutable
data class StatusCardIcons(
  val success: ImageVector,
  val info: ImageVector,
  val error: ImageVector,
  val retry: ImageVector,
)

object StatusCardDefaults {

  @Composable
  fun colors(
    container: Color = MaterialTheme.colorScheme.surface,
    title: Color = MaterialTheme.colorScheme.onSurface,
    subtitle: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    loadingIconContainer: Color = MaterialTheme.colorScheme.tertiaryContainer,
    loadingIndicator: Color = MaterialTheme.colorScheme.onTertiaryContainer,
    successIconContainer: Color = MaterialTheme.colorScheme.tertiaryContainer,
    successIcon: Color = MaterialTheme.colorScheme.onTertiaryContainer,
    infoIconContainer: Color = MaterialTheme.colorScheme.secondaryContainer,
    infoIcon: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    errorIconContainer: Color = MaterialTheme.colorScheme.errorContainer,
    errorIcon: Color = MaterialTheme.colorScheme.onErrorContainer,
    retryButtonContainer: Color = MaterialTheme.colorScheme.error,
    retryButtonIcon: Color = MaterialTheme.colorScheme.onError,
  ) = StatusCardColors(
    container = container,
    title = title,
    subtitle = subtitle,
    loadingIconContainer = loadingIconContainer,
    loadingIndicator = loadingIndicator,
    successIconContainer = successIconContainer,
    successIcon = successIcon,
    infoIconContainer = infoIconContainer,
    infoIcon = infoIcon,
    errorIconContainer = errorIconContainer,
    errorIcon = errorIcon,
    retryButtonContainer = retryButtonContainer,
    retryButtonIcon = retryButtonIcon,
  )

  fun icons(
    success: ImageVector = Icons.Default.Check,
    info: ImageVector = Icons.Default.Info,
    error: ImageVector = Icons.Default.Warning,
    retry: ImageVector = Icons.Default.Refresh,
  ) = StatusCardIcons(
    success = success,
    info = info,
    error = error,
    retry = retry,
  )
}

@Composable
fun StatusCard(
  state: StatusCardState,
  onRetry: () -> Unit,
  modifier: Modifier = Modifier,
  colors: StatusCardColors = StatusCardDefaults.colors(),
  icons: StatusCardIcons = StatusCardDefaults.icons(),
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = colors.container),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .animateContentSize()
        .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
      LeadingIcon(
        state = state,
        colors = colors,
        icons = icons,
      )
      HorizontalSpacer(12.dp)
      Texts(
        state = state,
        colors = colors,
        modifier = Modifier.weight(1f),
      )
      if (state is StatusCardState.Error && state.retryable) {
        HorizontalSpacer(12.dp)
        RetryButton(
          onClick = onRetry,
          colors = colors,
          icons = icons,
        )
      }
    }
  }
}

@Composable
private fun LeadingIcon(
  state: StatusCardState,
  colors: StatusCardColors,
  icons: StatusCardIcons,
) {
  val bgColor = when (state) {
    is StatusCardState.Loading -> colors.loadingIconContainer
    is StatusCardState.Success -> colors.successIconContainer
    is StatusCardState.Info -> colors.infoIconContainer
    is StatusCardState.Error -> colors.errorIconContainer
  }
  Box(
    contentAlignment = Alignment.Center,
    modifier = Modifier
      .size(40.dp)
      .clip(CircleShape)
      .background(bgColor),
  ) {
    when (state) {
      is StatusCardState.Loading -> {
        CircularProgressIndicator(
          color = colors.loadingIndicator,
          strokeWidth = 2.dp,
          modifier = Modifier.size(20.dp),
        )
      }

      is StatusCardState.Success -> {
        Icon(
          imageVector = icons.success,
          contentDescription = null,
          tint = colors.successIcon,
          modifier = Modifier.size(20.dp),
        )
      }

      is StatusCardState.Info -> {
        Icon(
          imageVector = icons.info,
          contentDescription = null,
          tint = colors.infoIcon,
          modifier = Modifier.size(20.dp),
        )
      }

      is StatusCardState.Error -> {
        Icon(
          imageVector = icons.error,
          contentDescription = null,
          tint = colors.errorIcon,
          modifier = Modifier.size(20.dp),
        )
      }
    }
  }
}

@Composable
private fun Texts(
  state: StatusCardState,
  colors: StatusCardColors,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier) {
    Text(
      text = state.title,
      fontSize = 14.sp,
      fontWeight = FontWeight.W600,
      color = colors.title,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
    val subtitle = state.subtitle
    if (subtitle != null) {
      Text(
        text = subtitle,
        fontSize = 12.sp,
        lineHeight = 14.sp,
        color = colors.subtitle,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun RetryButton(
  onClick: () -> Unit,
  colors: StatusCardColors,
  icons: StatusCardIcons,
) {
  Box(
    contentAlignment = Alignment.Center,
    modifier = Modifier
      .size(36.dp)
      .clip(CircleShape)
      .background(colors.retryButtonContainer)
      .clickable(onClick = onClick),
  ) {
    Icon(
      imageVector = icons.retry,
      contentDescription = null,
      tint = colors.retryButtonIcon,
      modifier = Modifier.size(18.dp),
    )
  }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F7)
@Composable
private fun StatusCardLoadingPreview() {
  MaterialTheme {
    Box(modifier = Modifier.padding(16.dp)) {
      StatusCard(
        state = StatusCardState.Loading(title = "Connecting… (9/28)"),
        onRetry = {},
      )
    }
  }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F7)
@Composable
private fun StatusCardSuccessPreview() {
  MaterialTheme {
    Box(modifier = Modifier.padding(16.dp)) {
      StatusCard(
        state = StatusCardState.Success(title = "Account prepared"),
        onRetry = {},
      )
    }
  }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F7)
@Composable
private fun StatusCardInfoPreview() {
  MaterialTheme {
    Box(modifier = Modifier.padding(16.dp)) {
      StatusCard(
        state = StatusCardState.Info(
          title = "New servers available",
          subtitle = "Check the updated locations list.",
        ),
        onRetry = {},
      )
    }
  }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F7)
@Composable
private fun StatusCardErrorPreview() {
  MaterialTheme {
    Box(modifier = Modifier.padding(16.dp)) {
      StatusCard(
        state = StatusCardState.Error(
          title = "Something went wrong",
          subtitle = "Check your connection.",
        ),
        onRetry = {},
      )
    }
  }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F7)
@Composable
private fun StatusCardErrorNoRetryPreview() {
  MaterialTheme {
    Box(modifier = Modifier.padding(16.dp)) {
      StatusCard(
        state = StatusCardState.Error(
          title = "Something went wrong",
          subtitle = "Check your connection.",
          retryable = false,
        ),
        onRetry = {},
      )
    }
  }
}
