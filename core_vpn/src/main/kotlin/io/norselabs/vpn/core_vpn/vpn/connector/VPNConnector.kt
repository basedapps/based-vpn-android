package io.norselabs.vpn.core_vpn.vpn.connector

import android.util.Base64
import arrow.core.Either
import arrow.core.flatMap
import com.google.gson.Gson
import io.norselabs.vpn.core_vpn.storage.CoreStorage
import io.norselabs.vpn.core_vpn.vpn.Credentials
import io.norselabs.vpn.core_vpn.vpn.Destination
import io.norselabs.vpn.core_vpn.vpn.Protocol
import io.norselabs.vpn.core_vpn.vpn.utils.ProfileDecoder
import io.norselabs.vpn.sdk.common.SdkError
import io.norselabs.vpn.sdk.dvpn_client.DVPNClient
import io.norselabs.vpn.sdk.services.connection.api.CredentialsResponse
import kotlinx.coroutines.CancellationException
import timber.log.Timber

class VPNConnector(
  private val gson: Gson,
  private val dvpn: DVPNClient,
  private val coreStorage: CoreStorage,
  private val driver: VPNDriver,
  listener: ConnectionLifecycleListener,
  clock: () -> Long = { System.currentTimeMillis() },
) {

  private val reporter = ConnectionLifecycleReporter(listener, clock)

  suspend fun connect(destination: Destination): Either<Error, Unit> {
    if (driver.isVpnConnected()) disconnect(DisconnectReason.SdkInitiated("reconnect"))

    val protocol = coreStorage.getVpnProtocol()
    val attemptId = reporter.start(destination, protocol)

    return try {
      fetchCredentials(destination, protocol)
        .onLeft { error -> reporter.credentialsFailed(error) }
        .flatMap { credentials ->
          reporter.credentialsReceived(credentials.serverId)
          connectVpn(credentials, attemptId, destination.toConnectionLabel())
        }
    } catch (ce: CancellationException) {
      reporter.cancelled(ce)
      throw ce
    }
  }

  fun disconnect(reason: DisconnectReason) {
    driver.stopVpn(reason)
    coreStorage.setCurrentServerId("")
    driver.resetNetworkClient()
    reporter.disconnect(reason)
  }

  private suspend fun fetchCredentials(
    destination: Destination,
    protocol: Protocol?,
  ): Either<Error, Credentials> {
    return when (destination) {
      is Destination.Deeplink -> parseDeeplink(destination)
      is Destination.Country -> getCredentials {
        dvpn.getCountryCredentials(destination.countryId, protocol?.strValue)
      }

      is Destination.City -> getCredentials {
        dvpn.getCityCredentials(destination.cityId, protocol?.strValue)
      }

      is Destination.Server -> getCredentials {
        dvpn.getServerCredentials(destination.serverId)
      }

      is Destination.Random -> getCredentials {
        dvpn.getQuickCredentials(protocol?.strValue)
      }
    }
  }

  private suspend fun getCredentials(
    request: suspend () -> Either<SdkError, CredentialsResponse>,
  ): Either<Error, Credentials> {
    return request()
      .flatMap(::parseCredentials)
      .mapLeft { parseError(it) }
  }

  private fun parseCredentials(data: CredentialsResponse): Either<SdkError, Credentials> {
    val protocol = data.protocol
    val privateKey = data.privateKey
    val uid = data.uid
    return when (protocol) {
      Protocol.WIREGUARD.strValue if privateKey != null ->
        Credentials.Wireguard(
          payload = data.payload,
          privateKey = privateKey,
          serverId = data.server.id,
        )

      Protocol.V2RAY.strValue if uid != null ->
        Credentials.V2Ray(
          payload = data.payload,
          uid = uid,
          serverId = data.server.id,
        )

      else -> null
    }
      ?.let { credentials -> Either.Right(credentials) }
      ?: Either.Left(SdkError.Unknown("Unknown protocol"))
  }

  private fun parseDeeplink(deeplink: Destination.Deeplink): Either<Error, Credentials> {
    return deeplink.url.split('/')
      .lastOrNull { it.isNotBlank() }
      ?.let { payload -> Either.Right(payload) }
      ?.map { Base64.decode(it, Base64.DEFAULT).decodeToString() }
      ?.flatMap(::parseCredentials)
      ?: Either.Left(Error.ParseCredentials)
  }

  private fun parseCredentials(payloadJson: String): Either<Error, Credentials> {
    return try {
      val isV2ray = payloadJson.contains("uid")
      val clazz = when {
        isV2ray -> Credentials.V2Ray::class.java
        else -> Credentials.Wireguard::class.java
      }
      val credentials = gson.fromJson(payloadJson, clazz)
      Either.Right(credentials)
    } catch (e: Exception) {
      Timber.tag(TAG).d("Parsing creation failed: $e")
      Either.Left(Error.ParseCredentials)
    }
  }

  private fun parseError(error: SdkError): Error {
    Timber.tag(TAG).d("Credentials creation failed: $error")
    val httpError = error as? SdkError.HttpError
    return when (httpError?.code) {
      401, 403, 425 -> Error.UserToken(httpError)
      else -> Error.GetCredentials(error)
    }
  }

  private suspend fun connectVpn(
    credentials: Credentials,
    attemptId: AttemptId,
    label: String?,
  ): Either<Error, Unit> {
    val profile = ProfileDecoder.decode(credentials, label)
    if (profile == null) {
      reporter.disconnect(DisconnectReason.InvalidProfile)
      return Either.Left(Error.StartV2Ray)
    }
    return driver.startVpn(profile, attemptId).fold(
      ifLeft = { error ->
        reporter.disconnect(DisconnectReason.TunnelEstablishFailed(error))
        Either.Left(Error.StartV2Ray)
      },
      ifRight = {
        reporter.tunnelUp()
        coreStorage.setCurrentServerId(credentials.serverId)
        driver.resetNetworkClient()
        Either.Right(Unit)
      },
    )
  }

  // Mirrors the user's selection granularity in the VPN notification subtitle.
  private fun Destination.toConnectionLabel(): String? = when (this) {
    is Destination.Country -> countryName
    is Destination.City -> "$countryName • $cityName"
    is Destination.Server -> "$cityName • $serverName"
    is Destination.Random, is Destination.Deeplink -> null
  }

  sealed interface Error {
    data class GetCredentials(val error: SdkError) : Error
    data object ParseCredentials : Error
    data class UserToken(val error: SdkError.HttpError) : Error
    data object StartV2Ray : Error
  }

  companion object {
    const val TAG = "VPNConnector"
  }
}
