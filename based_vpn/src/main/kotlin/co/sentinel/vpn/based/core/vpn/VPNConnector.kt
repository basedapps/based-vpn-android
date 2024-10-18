package co.sentinel.vpn.based.core.vpn

import arrow.core.Either
import arrow.core.flatMap
import co.sentinel.vpn.based.error.BaseError
import co.sentinel.vpn.based.storage.SelectedCity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber

class VPNConnector(
  private val interactor: VPNConnectorInteractor,
) {

  suspend fun connect(city: SelectedCity): Either<Error, Unit> {
    return withContext(Dispatchers.IO) {
      getCredentials(city)
        .onLeft { Timber.d("Failed to connect: $it") }
    }
  }

  fun disconnect() {
    if (interactor.isVpnConnected()) {
      interactor.stopVpn()
    }
  }

  fun isConnected(): Boolean {
    return interactor.isVpnConnected()
  }

  private suspend fun getCredentials(city: SelectedCity): Either<Error, Unit> {
    val protocol = interactor.getVpnProtocol()
      .takeIf { it != Protocol.NONE }
    return interactor.getCredentials(
      countryId = city.countryId,
      cityId = city.id,
      protocol = protocol,
    )
      .mapLeft { parseError(it) }
      .flatMap { credentials ->
        connectVpn(
          serverId = city.serverId,
          credentials = credentials,
        )
      }
  }

  private fun parseError(exception: Exception): Error {
    return when (exception) {
      is HttpException -> {
        val response = exception.response()
        when (response?.code()) {
          401, 403, 425 -> Error.UserToken
          else -> Error.GetCredentials(
            response?.run { "$this ${errorBody()?.string()}" },
          )
        }
      }

      else -> Error.GetCredentials(exception.message)
    }
  }

  private suspend fun connectVpn(
    serverId: String,
    credentials: Credentials,
  ): Either<Error, Unit> {
    interactor.startVpn(
      serverId = serverId,
      credentials = credentials,
    ).getOrNull()
      ?: return Either.Left(Error.StartV2Ray)

    interactor.resetConnection()

    return Either.Right(Unit)
  }

  sealed interface Error : BaseError {

    data class GetCredentials(val error: String?) : Error {
      override val message: String = "Get Credentials error: ${error ?: "Unknown"}"
    }

    data object UserToken : Error {
      override val message: String = "User token error"
    }

    data object ParseProfile : Error {
      override val message: String = "Parse profile error"
    }

    data object StartV2Ray : Error {
      override val message: String = "Start V2Ray error"
    }

    data object QuickConnection : Error {
      override val message: String = "Quick Connection failed"
    }
  }
}
