package co.sentinel.vpn.based.vpn

import arrow.core.Either
import arrow.core.flatMap
import co.sentinel.vpn.based.error.BaseError
import co.sentinel.vpn.based.network.model.Credentials
import co.sentinel.vpn.based.network.model.Protocol
import co.sentinel.vpn.based.network.repository.BasedRepository
import co.sentinel.vpn.based.storage.BasedStorage
import co.sentinel.vpn.based.storage.LogsStorage
import co.sentinel.vpn.based.storage.SelectedCity
import co.sentinel.vpn.v2ray.model.VpnProfile
import co.sentinel.vpn.v2ray.repo.V2RayRepository
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class VPNConnector @Inject constructor(
  private val repository: BasedRepository,
  private val v2RayRepository: V2RayRepository,
  private val storage: BasedStorage,
  private val logsStorage: LogsStorage,
) {

  suspend fun connect(city: SelectedCity): Either<Error, Unit> {
    return withContext(Dispatchers.IO) {
      getCredentials(city)
        .onLeft { logsStorage.writeToLog(it) }
    }
  }

  suspend fun disconnect() {
    if (v2RayRepository.isConnected()) {
      v2RayRepository.stopV2ray()
    }
  }

  fun isConnected(): Boolean {
    return v2RayRepository.isConnected()
  }

  private suspend fun getCredentials(city: SelectedCity): Either<Error, Unit> {
    val protocol = storage.getVpnProtocol()
      .takeIf { it != Protocol.NONE }
    return repository.getCredentials(
      countryId = city.countryId,
      cityId = city.id,
      protocol = protocol,
    )
      .mapLeft { parseError(it) }
      .flatMap { credentials ->
        getVPNProfile(
          serverId = city.serverId,
          credentials = credentials.data,
        )
      }
  }

  private fun parseError(exception: Exception): Error {
    return when (exception) {
      is HttpException -> {
        val response = exception.response()
        when (response?.code()) {
          401 -> Error.TokenExpired
          403 -> Error.Banned
          425 -> Error.NotEnrolled
          else -> Error.GetCredentials(
            response?.run { "$this ${errorBody()?.string()}" },
          )
        }
      }

      else -> Error.GetCredentials(exception.message)
    }
  }

  private suspend fun getVPNProfile(
    serverId: String,
    credentials: Credentials,
  ): Either<Error, Unit> {
    val profile = when (credentials.protocol) {
      Protocol.WIREGUARD -> ProfileDecoder.decodeWireguard(
        privateKey = credentials.privateKey,
        payload = credentials.payload,
      )

      Protocol.V2RAY -> ProfileDecoder.decodeVmess(
        payload = credentials.payload,
        uid = credentials.privateKey,
      )

      else -> throw Exception("Unknown protocol")
    }
    return connectV2Ray(serverId, profile)
  }

  private suspend fun connectV2Ray(
    serverId: String,
    profile: VpnProfile?,
  ): Either<Error, Unit> {
    profile ?: return Either.Left(Error.ParseProfile)
    v2RayRepository.startV2Ray(
      profile = profile,
      serverId = serverId,
    ).getOrNull()
      ?: return Either.Left(Error.StartV2Ray)

    repository.resetConnection()

    return Either.Right(Unit)
  }

  sealed interface Error : BaseError {
    data class GetCredentials(val error: String?) : Error {
      override val message: String = "Get Credentials error: ${error ?: "Unknown"}"
    }

    data object NotEnrolled : Error {
      override val message: String = "User not enrolled"
    }

    data object Banned : Error {
      override val message: String = "User has been banned"
    }

    data object TokenExpired : Error {
      override val message: String = "Token has been expired"
    }

    data object ParseProfile : Error {
      override val message: String = "Parse profile error"
    }

    data object CreateTunnel : Error {
      override val message: String = "Create Wireguard tunnel error"
    }

    data object SetTunnelState : Error {
      override val message: String = "Set Wireguard tunnel state error"
    }

    data object StartV2Ray : Error {
      override val message: String = "Start V2Ray error"
    }

    data object QuickConnection : Error {
      override val message: String = "Quick Connection failed"
    }
  }
}
