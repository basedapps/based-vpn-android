package io.norselabs.vpn.based.core_impl.vpn

import arrow.core.Either
import io.norselabs.vpn.core_vpn.vpn.connector.VPNInteractor
import io.norselabs.vpn.sdk.dvpn_client.DVPNClient
import io.norselabs.vpn.v2ray.error.V2RayError
import io.norselabs.vpn.v2ray.model.VpnProfile
import io.norselabs.vpn.v2ray.repo.V2RayRepository
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class VPNInteractorImpl(
  private val dvpnClient: DVPNClient,
  private val v2RayRepository: V2RayRepository,
) : VPNInteractor {

  override suspend fun startVpn(vpnProfile: VpnProfile): Either<V2RayError, Unit> {
    return v2RayRepository.startV2Ray(vpnProfile)
  }

  override fun isVpnConnected(): Boolean {
    return v2RayRepository.isConnected()
  }

  override fun stopVpn() {
    v2RayRepository.stopV2ray()
  }

  @OptIn(DelicateCoroutinesApi::class)
  override fun resetNetworkClient() {
    GlobalScope.launch(Dispatchers.IO) {
      dvpnClient.resetConnectionPool()
    }
  }
}
