package io.norselabs.vpn.core_vpn.vpn.connector

import arrow.core.Either
import io.norselabs.vpn.v2ray.error.V2RayError
import io.norselabs.vpn.v2ray.model.VpnProfile

interface VPNInteractor {

  suspend fun startVpn(vpnProfile: VpnProfile): Either<V2RayError, Unit>
  fun isVpnConnected(): Boolean
  fun stopVpn()
  fun resetNetworkClient()
}
