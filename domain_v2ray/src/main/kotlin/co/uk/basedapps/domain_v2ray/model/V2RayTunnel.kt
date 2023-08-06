package co.uk.basedapps.domain_v2ray.model

import co.uk.basedapps.domain.models.VpnTunnel

data class V2RayTunnel(
  override val name: String = "V2Ray Tunnel",
  override var state: VpnTunnel.State = VpnTunnel.State.UP,
  override var serverId: String?,
  override var subscriptionId: Long? = null,
) : VpnTunnel
