package co.sentinel.vpn.based.vpn

import co.sentinel.vpn.v2ray.repo.V2RayRepository
import co.sentinel.vpn.wireguard.repo.WireguardRepository
import javax.inject.Inject

class DdsConfigurator @Inject constructor(
  private val wireguardRepository: WireguardRepository,
  private val v2RayRepository: V2RayRepository,
) {

  suspend fun getDefaultDns(): Dns {
    val currentDns = wireguardRepository.getDefaultDns()
    return Dns.entries.first { it.address == currentDns }
  }

  suspend fun setDns(dns: Dns) {
    wireguardRepository.updateDns(dns.address)
    v2RayRepository.updateDns(dns.address)
  }

  enum class Dns(
    val address: String,
  ) {
    Cloudflare("1.1.1.1, 1.0.0.1"),
    Google("8.8.8.8, 8.8.4.4"),
    Handshake("103.196.38.38, 103.196.38.39, 103.196.38.40"),
  }
}
