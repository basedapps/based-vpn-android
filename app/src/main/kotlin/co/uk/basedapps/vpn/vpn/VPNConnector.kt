package co.uk.basedapps.vpn.vpn

import co.uk.basedapps.domain.functional.Either
import co.uk.basedapps.domain.functional.getOrNull
import co.uk.basedapps.domain.functional.requireRight
import co.uk.basedapps.domain.models.VpnTunnel
import co.uk.basedapps.domain_v2ray.V2RayRepository
import co.uk.basedapps.domain_v2ray.model.V2RayVpnProfile
import co.uk.basedapps.domain_wireguard.core.init.DefaultTunnelName
import co.uk.basedapps.domain_wireguard.core.model.WireguardVpnProfile
import co.uk.basedapps.domain_wireguard.core.repo.WireguardRepository
import co.uk.basedapps.vpn.common.decodeV2RayVpnProfile
import co.uk.basedapps.vpn.common.decodeWireguardVpnProfile
import co.uk.basedapps.vpn.network.BasedRepository
import co.uk.basedapps.vpn.network.Credentials
import co.uk.basedapps.vpn.network.Protocol
import co.uk.basedapps.vpn.storage.SelectedCity
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VPNConnector @Inject constructor(
  private val repository: BasedRepository,
  private val wireguardRepository: WireguardRepository,
  private val v2RayRepository: V2RayRepository,
) {

  suspend fun connect(city: SelectedCity): Either<Unit, Unit> {
    return withContext(Dispatchers.IO) {
      getCredentials(city)
    }
  }

  suspend fun disconnect() {
    return withContext(Dispatchers.IO) {
      when {
        wireguardRepository.isConnected() -> disconnectWireguard()
        v2RayRepository.isConnected() -> disconnectV2Ray()
      }
    }
  }

  suspend fun getConnection(): VpnTunnel? {
    return withContext(Dispatchers.IO) {
      when {
        wireguardRepository.isConnected() -> wireguardRepository.getTunnel()
        v2RayRepository.isConnected() -> v2RayRepository.getTunnel()
        else -> null
      }
    }
  }

  private suspend fun getCredentials(city: SelectedCity): Either<Unit, Unit> {
    val credentialsRes = repository.getCredentials(
      countryId = city.countryId,
      cityId = city.id,
    ).getOrNull()
    credentialsRes ?: return Either.Left(Unit)

    return getVPNProfile(
      serverId = city.serverId,
      credentials = credentialsRes.data,
    )
  }

  private suspend fun getVPNProfile(
    serverId: String,
    credentials: Credentials,
  ): Either<Unit, Unit> {
    return when (credentials.protocol) {
      Protocol.WIREGUARD -> connectWireguard(
        serverId = serverId,
        privateKey = credentials.privateKey,
        profile = decodeWireguardVpnProfile(credentials.payload),
      )

      Protocol.V2RAY -> connectV2Ray(
        serverId = serverId,
        profile = decodeV2RayVpnProfile(
          payload = credentials.payload,
          uid = credentials.privateKey,
        ),
      )
    }
  }

  private suspend fun connectWireguard(
    serverId: String,
    privateKey: String,
    profile: WireguardVpnProfile?,
  ): Either<Unit, Unit> {
    profile ?: return Either.Left(Unit)
    val keyPair = wireguardRepository.generateKeyPair(privateKey)
    val createTunnelRes = wireguardRepository.createOrUpdate(
      vpnProfile = profile,
      keyPair = keyPair,
      serverId = serverId,
    )
    createTunnelRes
      .getOrNull()
      ?: return Either.Left(Unit)

    wireguardRepository.setTunnelState(
      tunnelName = createTunnelRes.requireRight().name,
      tunnelState = VpnTunnel.State.UP,
    ).getOrNull()
      ?: return Either.Left(Unit)

    repository.resetConnection()

    return Either.Right(Unit)
  }

  private suspend fun connectV2Ray(
    serverId: String,
    profile: V2RayVpnProfile?,
  ): Either<Unit, Unit> {
    profile ?: return Either.Left(Unit)
    v2RayRepository.startV2Ray(
      profile = profile,
      serverId = serverId,
      serverName = "BasedVPN server",
    ).getOrNull()
      ?: return Either.Left(Unit)

    repository.resetConnection()

    return Either.Right(Unit)
  }

  private fun disconnectV2Ray() {
    v2RayRepository.stopV2ray()
  }

  private suspend fun disconnectWireguard() {
    val tunnel = wireguardRepository
      .getTunnel(DefaultTunnelName)
      ?: return
    wireguardRepository.setTunnelState(
      tunnelName = tunnel.name,
      tunnelState = VpnTunnel.State.DOWN,
    )
  }
}
