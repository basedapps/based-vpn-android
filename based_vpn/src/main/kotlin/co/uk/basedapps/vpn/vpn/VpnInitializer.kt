package co.uk.basedapps.vpn.vpn

import android.content.Context
import co.sentinel.vpn.v2ray.control.V2RayInitializer
import co.sentinel.vpn.wireguard.control.WireguardInitializer
import javax.inject.Inject

class VpnInitializer @Inject constructor(
  private val wireguardInitializer: WireguardInitializer,
) {

  fun setupVPN(appContext: Context) {
    V2RayInitializer.init(appContext)
    wireguardInitializer.init()
  }
}
