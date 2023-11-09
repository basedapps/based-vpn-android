package co.uk.basedapps.vpn.vpn

import android.content.Context
import co.uk.basedapps.domain_wireguard.core.init.WireguardInitializer
import com.v2ray.ang.V2RayInitializer
import javax.inject.Inject

class VpnInitializer @Inject constructor(
  private val wireguardInitializer: WireguardInitializer,
) {

  fun setupVPN(appContext: Context) {
    V2RayInitializer.init(appContext)
    wireguardInitializer.init()
  }
}
