package io.norselabs.vpn.core_vpn.vpn.dns

import com.v2ray.ang.util.models.DnsServers
import io.norselabs.vpn.v2ray.repo.V2RayRepository

class DnsConfigurator(
  private val v2Ray: V2RayRepository,
) {

  fun init() {
    if (v2Ray.getDns() is DnsServers.Default) {
      setDns(DnsProvider.Quad9)
    }
  }

  fun getDefaultDns(): DnsProvider {
    val currentDns = v2Ray.getDns().servers.joinToString(",")
    val selectedDnsProvider = DnsProvider.entries.firstOrNull { it.servers == currentDns }
    if (selectedDnsProvider == null) {
      setDns(DnsProvider.Quad9)
    }
    return selectedDnsProvider ?: DnsProvider.Quad9
  }

  fun setDns(dnsProvider: DnsProvider) {
    v2Ray.updateDns(dnsProvider.servers)
  }
}

enum class DnsProvider(val servers: String) {
  Cloudflare("1.1.1.1,1.0.0.1"),
  Google("8.8.8.8,8.8.4.4"),
  Quad9("9.9.9.9,149.112.112.112"),
}
