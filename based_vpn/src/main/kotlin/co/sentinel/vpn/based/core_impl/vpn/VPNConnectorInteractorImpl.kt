package co.sentinel.vpn.based.core_impl.vpn

import arrow.core.Either
import arrow.core.flatMap
import co.sentinel.vpn.based.core.vpn.Credentials
import co.sentinel.vpn.based.core.vpn.Destination
import co.sentinel.vpn.based.core.vpn.Protocol
import co.sentinel.vpn.based.core.vpn.VPNConnectorInteractor
import co.sentinel.vpn.based.network.repository.BasedRepository
import co.sentinel.vpn.based.storage.BasedStorage
import co.sentinel.vpn.based.vpn.ProfileDecoder
import io.norselabs.vpn.v2ray.error.V2RayError
import io.norselabs.vpn.v2ray.repo.V2RayRepository
import retrofit2.HttpException

class VPNConnectorInteractorImpl(
  private val repository: BasedRepository,
  private val v2RayRepository: V2RayRepository,
  private val storage: BasedStorage,
) : VPNConnectorInteractor {

  override suspend fun getCredentials(
    destination: Destination,
    protocol: Protocol?,
  ): Either<Exception, Credentials> {
    val city = (destination as? Destination.City)
      ?: return Either.Left(Exception("Destination.Server is not supported yet"))
    return repository.getCredentials(city.countryId, city.cityId, protocol)
      .flatMap { response ->
        val data = response.data
        when (data.protocol) {
          Protocol.WIREGUARD -> Credentials.Wireguard(
            payload = data.payload,
            privateKey = data.privateKey,
          )

          Protocol.V2RAY -> Credentials.V2Ray(
            payload = data.payload,
            uid = data.privateKey,
          )

          Protocol.NONE -> null
        }
          ?.let { credentials -> Either.Right(credentials) }
          ?: Either.Left(Exception("Unknown protocol"))
      }
  }

  override suspend fun resetConnection() {
    repository.resetConnection()
  }

  override fun parseHttpCode(exception: Exception): Int? {
    return (exception as? HttpException)?.response()?.code()
  }

  override suspend fun startVpn(
    serverId: String,
    credentials: Credentials,
  ): Either<V2RayError, Unit> {
    val profile = when (credentials) {
      is Credentials.Wireguard -> ProfileDecoder.decodeWireguard(
        privateKey = credentials.privateKey,
        payload = credentials.payload,
      )

      is Credentials.V2Ray -> ProfileDecoder.decodeVmess(
        payload = credentials.payload,
        uid = credentials.uid,
      )
    }
    return when {
      profile != null -> v2RayRepository.startV2Ray(profile, serverId)
      else -> Either.Left(V2RayError.StartV2Ray)
    }
  }

  override fun isVpnConnected(): Boolean {
    return v2RayRepository.isConnected()
  }

  override fun stopVpn() {
    v2RayRepository.stopV2ray()
  }

  override fun getVpnProtocol(): Protocol {
    return storage.getVpnProtocol()
  }
}
