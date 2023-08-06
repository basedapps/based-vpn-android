package co.uk.basedapps.domain_wireguard.core.model

data class TunnelPeer(
  val allowedIps: String,
  val endpoint: String?,
  val persistentKeepAlive: String?,
  val preSharedKey: String?,
  val publicKey: String,
)
