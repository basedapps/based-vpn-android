package co.sentinel.vpn.based.core.vpn

import arrow.core.Either
import arrow.core.flatMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class VPNConnector(
  private val interactor: VPNConnectorInteractor,
) {

  suspend fun connect(destination: Destination): Either<Error, Unit> {
    return withContext(Dispatchers.IO) {
      getCredentials(destination)
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

  private suspend fun getCredentials(destination: Destination): Either<Error, Unit> {
    val protocol = interactor.getVpnProtocol()
      .takeIf { it != Protocol.NONE }
    return interactor.getCredentials(
      destination = destination,
      protocol = protocol,
    )
      .mapLeft { parseError(it) }
      .flatMap { credentials ->
        connectVpn(
          serverId = destination.id,
          credentials = credentials,
        )
      }
  }

  private fun parseError(exception: Exception): Error {
    val code = interactor.parseHttpCode(exception)
    return when (code) {
      401, 403, 425 -> Error.UserToken
      else -> Error.GetCredentials(exception)
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

  sealed interface Error {
    data class GetCredentials(val exception: Exception) : Error
    data object UserToken : Error
    data object StartV2Ray : Error
    data object QuickConnection : Error
  }
}
