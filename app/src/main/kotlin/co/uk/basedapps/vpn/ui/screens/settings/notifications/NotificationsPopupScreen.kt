package co.uk.basedapps.vpn.ui.screens.settings.notifications

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import co.uk.basedapps.vpn.R
import co.uk.basedapps.vpn.ui.screens.settings.LocalSettingsScreenViewModel
import io.norselabs.vpn.common_compose.prompt_sheet.PromptSheetContent

// The Settings entry point deliberately doesn't touch NotificationPromptStorage:
// the row stays visible regardless, so there is no "prompt shown" flag to record.
class NotificationsPopupScreen : Screen {

  @Composable
  override fun Content() {
    val viewModel = LocalSettingsScreenViewModel.current
    val navigator = LocalBottomSheetNavigator.current

    PromptSheetContent(
      title = stringResource(R.string.notifications_popup_title),
      description = stringResource(R.string.notifications_popup_description),
      confirmLabel = stringResource(R.string.notifications_popup_confirm),
      onConfirmClick = {
        viewModel.onNotificationsPopupConfirm()
        navigator.hide()
      },
      dismissLabel = stringResource(R.string.notifications_popup_later),
      onDismissClick = { navigator.hide() },
    )
  }
}
