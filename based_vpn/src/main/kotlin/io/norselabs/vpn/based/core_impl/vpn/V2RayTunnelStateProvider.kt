package io.norselabs.vpn.based.core_impl.vpn

import io.norselabs.vpn.sdk.common.tunnel.TunnelStateProvider
import io.norselabs.vpn.v2ray.repo.V2RayRepository

/**
 * Bridges the v2ray tunnel state into dvpn_sdk, so `getIpData()` can egress
 * through the tunnel's local SOCKS5 proxy while connected
 * (see SolarLabs/docs/tunnel-routing-policy.md).
 */
class V2RayTunnelStateProvider(
  private val v2Ray: V2RayRepository,
) : TunnelStateProvider {

  override fun isTunnelConnected(): Boolean = v2Ray.isConnected()

  override fun getTunnelSocksPort(): Int = v2Ray.getSocksProxyPort()
}
