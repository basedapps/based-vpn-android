package co.uk.basedapps.vpn.ui.screens.dashboard.popup

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import co.uk.basedapps.vpn.R
import co.uk.basedapps.vpn.ui.screens.dashboard.LocalDashboardScreenViewModel
import io.norselabs.vpn.common_compose.prompt_sheet.PromptSheetContent

class NotificationsPopupScreen : Screen {

  @Composable
  override fun Content() {
    val viewModel = LocalDashboardScreenViewModel.current
    val navigator = LocalBottomSheetNavigator.current

    // A swipe/scrim dismiss deliberately calls nothing: the prompt isn't
    // recorded as shown and the connect flow stops — the next Connect tap
    // shows the popup again.
    PromptSheetContent(
      title = stringResource(R.string.notifications_popup_title),
      description = stringResource(R.string.notifications_popup_description),
      confirmLabel = stringResource(R.string.notifications_popup_confirm),
      onConfirmClick = {
        viewModel.onNotificationsPopupConfirm()
        navigator.hide()
      },
      dismissLabel = stringResource(R.string.notifications_popup_later),
      onDismissClick = {
        viewModel.onNotificationsPopupDismiss()
        navigator.hide()
      },
    )
  }
}
