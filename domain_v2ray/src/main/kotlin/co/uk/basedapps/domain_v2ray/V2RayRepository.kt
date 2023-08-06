package co.uk.basedapps.domain_v2ray

import co.uk.basedapps.domain.functional.Either
import co.uk.basedapps.domain.models.VpnTunnel
import co.uk.basedapps.domain_v2ray.model.V2RayVpnProfile

interface V2RayRepository {

  suspend fun startV2Ray(
    profile: V2RayVpnProfile,
    serverId: String,
    serverName: String,
  ): Either<Unit, Unit>

  fun stopV2ray()

  fun isConnected(): Boolean

  suspend fun getTunnel(tunnelName: String = ""): VpnTunnel?
}
