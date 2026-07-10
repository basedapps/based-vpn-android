package co.uk.basedapps.vpn.ui.screens.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.hilt.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import co.uk.basedapps.vpn.ui.theme.BasedAppColor
import co.uk.basedapps.vpn.ui.widget.BasedButton
import co.uk.basedapps.vpn.ui.widget.ButtonSize
import co.uk.basedapps.vpn.ui.widget.ButtonStyle
import co.uk.basedapps.vpn.ui.widget.TopBar
import io.norselabs.vpn.common.status_card.StatusCardController
import io.norselabs.vpn.common_compose.status_card.StatusCardHost

class StatusCardDemoScreen : Screen {

  @Composable
  override fun Content() {
    val viewModel = getScreenModel<StatusCardDemoViewModel>()
    val navigator = LocalNavigator.currentOrThrow

    StatusCardDemoScreenStateless(
      statusCard = viewModel.statusCard,
      onBackClick = navigator::pop,
      onScenarioClick = viewModel::onScenarioClick,
      onLoadingClick = viewModel::onLoadingClick,
      onSuccessClick = viewModel::onSuccessClick,
      onInfoClick = viewModel::onInfoClick,
      onErrorClick = viewModel::onErrorClick,
      onHideClick = viewModel::onHideClick,
    )
  }
}

@Composable
fun StatusCardDemoScreenStateless(
  statusCard: StatusCardController,
  onBackClick: () -> Unit,
  onScenarioClick: () -> Unit,
  onLoadingClick: () -> Unit,
  onSuccessClick: () -> Unit,
  onInfoClick: () -> Unit,
  onErrorClick: () -> Unit,
  onHideClick: () -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(BasedAppColor.Background),
  ) {
    TopBar(
      title = "StatusCard Demo",
      navigateBack = onBackClick,
    )
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
      ) {
        DemoButton(
          text = "Play scenario",
          style = ButtonStyle.Primary,
          onClick = onScenarioClick,
        )
        Spacer(modifier = Modifier.size(16.dp))
        DemoButton(
          text = "Show loading",
          style = ButtonStyle.Secondary,
          onClick = onLoadingClick,
        )
        Spacer(modifier = Modifier.size(8.dp))
        DemoButton(
          text = "Show success",
          style = ButtonStyle.Secondary,
          onClick = onSuccessClick,
        )
        Spacer(modifier = Modifier.size(8.dp))
        DemoButton(
          text = "Show info",
          style = ButtonStyle.Secondary,
          onClick = onInfoClick,
        )
        Spacer(modifier = Modifier.size(8.dp))
        DemoButton(
          text = "Show error",
          style = ButtonStyle.Secondary,
          onClick = onErrorClick,
        )
        Spacer(modifier = Modifier.size(8.dp))
        DemoButton(
          text = "Hide",
          style = ButtonStyle.Secondary,
          onClick = onHideClick,
        )
      }
      StatusCardHost(controller = statusCard)
    }
  }
}

@Composable
private fun DemoButton(
  text: String,
  style: ButtonStyle,
  onClick: () -> Unit,
) {
  BasedButton(
    text = text,
    style = style,
    size = ButtonSize.M,
    onClick = onClick,
    modifier = Modifier.fillMaxWidth(),
  )
}
