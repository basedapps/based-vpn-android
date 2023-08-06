package co.uk.basedapps.domain_wireguard.core.model

data class TunnelConfig(
  val tunnelInterface: TunnelInterface,
  val peers: List<TunnelPeer> = ArrayList(),
)
