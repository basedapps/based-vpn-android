package co.uk.basedapps.domain_v2ray.model

import co.uk.basedapps.domain.models.VpnProfile

data class V2RayVpnProfile(
  val uid: String,
  val address: String,
  val listenPort: String,
  val transport: String,
) : VpnProfile
