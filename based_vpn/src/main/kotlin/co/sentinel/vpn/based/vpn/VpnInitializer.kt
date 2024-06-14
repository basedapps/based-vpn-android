package co.sentinel.vpn.based.vpn

import android.content.Context
import co.sentinel.vpn.v2ray.control.V2RayInitializer

object VpnInitializer {

  fun setupVPN(appContext: Context) {
    V2RayInitializer.init(appContext)
  }
}
