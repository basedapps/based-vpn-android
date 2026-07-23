package io.norselabs.vpn.common_compose.permissions

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import io.norselabs.vpn.common.ext.openNotificationSettings
import io.norselabs.vpn.common.permissions.NotificationPermissionChecker
import io.norselabs.vpn.common.permissions.NotificationPermissionStatus

@Stable
class NotificationPermissionState internal constructor(
  status: State<NotificationPermissionStatus>,
  private val launchSystemRequest: () -> Unit,
  private val launchSettings: () -> Unit,
) {

  val status: NotificationPermissionStatus by status

  /**
   * Runs the action that can actually enable notifications for the current [status]:
   * the system permission dialog for [NotificationPermissionStatus.Denied], the app's
   * notification settings screen for [NotificationPermissionStatus.Disabled].
   */
  fun request() {
    when (status) {
      NotificationPermissionStatus.Denied -> launchSystemRequest()
      NotificationPermissionStatus.Disabled -> launchSettings()
      NotificationPermissionStatus.Granted -> Unit
    }
  }

  /** Opens the app's notification settings screen (e.g. after a permanently denied request). */
  fun openSettings() = launchSettings()
}

/**
 * Self-contained notification permission state: checks on composition and on every resume,
 * requests via [NotificationPermissionState.request]. With no [checker] argument a local
 * one is created — enough while only the UI needs the status. If the app also reads the
 * status from ViewModels, provide a single [NotificationPermissionChecker] in DI and pass
 * it here from every call site, so all refresh triggers land in the same [StateFlow].
 */
@Composable
fun rememberNotificationPermissionState(
  onResult: (isGranted: Boolean) -> Unit = {},
  checker: NotificationPermissionChecker = rememberNotificationPermissionChecker(),
): NotificationPermissionState {
  val context = LocalContext.current
  val currentOnResult by rememberUpdatedState(onResult)
  val launcher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission(),
  ) { isGranted ->
    checker.refresh()
    currentOnResult(isGranted)
  }
  LifecycleResumeEffect(checker) {
    checker.refresh()
    onPauseOrDispose { }
  }
  val status = checker.status.collectAsState()
  return remember(checker) {
    NotificationPermissionState(
      status = status,
      launchSystemRequest = { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) },
      launchSettings = { context.openNotificationSettings() },
    )
  }
}

@Composable
fun rememberNotificationPermissionChecker(): NotificationPermissionChecker {
  val appContext = LocalContext.current.applicationContext
  return remember { NotificationPermissionChecker(appContext) }
}
