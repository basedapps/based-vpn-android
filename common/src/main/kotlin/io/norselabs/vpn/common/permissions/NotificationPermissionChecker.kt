package io.norselabs.vpn.common.permissions

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NotificationPermissionChecker(private val context: Context) {

  private val _status = MutableStateFlow(check())
  val status: StateFlow<NotificationPermissionStatus> = _status.asStateFlow()

  fun refresh(): NotificationPermissionStatus = check().also { _status.value = it }

  private fun check(): NotificationPermissionStatus = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
      context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED ->
      NotificationPermissionStatus.Denied

    !context.getSystemService(NotificationManager::class.java).areNotificationsEnabled() ->
      NotificationPermissionStatus.Disabled

    else -> NotificationPermissionStatus.Granted
  }
}

sealed interface NotificationPermissionStatus {
  /** Notifications can be posted. */
  data object Granted : NotificationPermissionStatus

  /** API 33+: the POST_NOTIFICATIONS runtime permission is not granted — a system dialog can be requested. */
  data object Denied : NotificationPermissionStatus

  /** API 26–32: notifications are toggled off in system settings — no system dialog exists, only the settings screen. */
  data object Disabled : NotificationPermissionStatus
}
