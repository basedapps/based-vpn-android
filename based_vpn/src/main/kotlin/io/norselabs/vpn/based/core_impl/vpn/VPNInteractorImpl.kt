package io.norselabs.vpn.based.core_impl.vpn

import arrow.core.Either
import io.norselabs.vpn.core_vpn.vpn.connector.VPNInteractor
import io.norselabs.vpn.v2ray.error.V2RayError
import io.norselabs.vpn.v2ray.model.VpnProfile
import io.norselabs.vpn.v2ray.repo.V2RayRepository

class VPNInteractorImpl(
  private val v2RayRepository: V2RayRepository,
) : VPNInteractor {

  override suspend fun resetConnection() {
    // TODO: is it still necessary with Ktor client?
//    withContext(Dispatchers.IO) {
//      repository.resetConnection()
//    }
  }

  override suspend fun startVpn(vpnProfile: VpnProfile): Either<V2RayError, Unit> {
    return v2RayRepository.startV2Ray(vpnProfile)
  }

  override fun isVpnConnected(): Boolean {
    return v2RayRepository.isConnected()
  }

  override fun stopVpn() {
    v2RayRepository.stopV2ray()
  }
}
