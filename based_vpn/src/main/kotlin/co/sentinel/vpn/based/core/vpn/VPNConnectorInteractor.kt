package co.sentinel.vpn.based.core.vpn

import arrow.core.Either
import io.norselabs.vpn.v2ray.error.V2RayError

interface VPNConnectorInteractor {

  // Repository
  suspend fun getCredentials(
    destination: Destination,
    protocol: Protocol?,
  ): Either<Exception, Credentials>

  suspend fun resetConnection()

  fun parseHttpCode(exception: Exception): Int?

  // VPN
  suspend fun startVpn(
    serverId: String,
    credentials: Credentials,
  ): Either<V2RayError, Unit>

  fun isVpnConnected(): Boolean
  fun stopVpn()

  // Storage
  fun getVpnProtocol(): Protocol
}
