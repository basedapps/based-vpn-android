package co.uk.basedapps.domain_wireguard.core.model

import co.uk.basedapps.domain.models.VpnTunnel

data class WireguardTunnel(
  override val name: String,
  override var state: VpnTunnel.State,
  var config: TunnelConfig? = null,
  override var subscriptionId: Long? = null,
  override var serverId: String? = null,
  var duration: Long? = null,
) : VpnTunnel
