package io.norselabs.vpn.common_compose

import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberDeviceType(): Device {
  val context = LocalContext.current
  return remember {
    when (context.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)) {
      true -> Device.TV
      false -> Device.Phone
    }
  }
}

enum class Device {
  Phone,
  TV,
}
