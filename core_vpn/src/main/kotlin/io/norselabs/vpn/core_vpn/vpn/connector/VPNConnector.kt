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
import timber.log.Timber

class VPNConnector(
  private val gson: Gson,
  private val dvpn: DVPNClient,
  private val coreStorage: CoreStorage,
  private val interactor: VPNInteractor,
) {

  suspend fun connect(destination: Destination): Either<Error, Unit> {
    if (interactor.isVpnConnected()) disconnect()
    return when (destination) {
      is Destination.Deeplink -> parseDeeplink(destination)
      else -> getCredentials(destination)
    }.flatMap { connectVpn(it) }
  }

  fun disconnect() {
    interactor.stopVpn()
    coreStorage.setCurrentServerId("")
  }

  private suspend fun getCredentials(destination: Destination): Either<Error, Credentials> {
    val protocol = coreStorage.getVpnProtocol()
      .takeIf { it != Protocol.NONE }
    return getCredentials(
      destination = destination,
      protocol = protocol,
    )
      .mapLeft { parseError(it) }
  }

  suspend fun getCredentials(
    destination: Destination,
    protocol: Protocol?,
  ): Either<SdkError, Credentials> {
    val city = (destination as? Destination.City)
      ?: return Either.Left(SdkError.Unknown("Destination.Server is not supported yet"))
    return dvpn.getCredentials(city.cityId, protocol?.strValue)
      .flatMap { data ->
        val protocol = data.protocol
        val privateKey = data.privateKey
        val uid = data.uid
        when {
          protocol == Protocol.WIREGUARD.strValue && privateKey != null ->
            Credentials.Wireguard(
              payload = data.payload,
              privateKey = privateKey,
              serverId = data.server.id,
            )

          protocol == Protocol.V2RAY.strValue && uid != null ->
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
    val code = (error as? SdkError.HttpError)?.code
    return when (code) {
      401, 403, 425 -> Error.UserToken
      else -> Error.GetCredentials(error)
    }
  }

  private suspend fun connectVpn(credentials: Credentials): Either<Error, Unit> {
    ProfileDecoder.decode(credentials)?.let { profile ->
      interactor.startVpn(profile).getOrNull()
    }
      ?: return Either.Left(Error.StartV2Ray)

    coreStorage.setCurrentServerId(credentials.serverId)

    interactor.resetConnection()

    return Either.Right(Unit)
  }

  sealed interface Error {
    data class GetCredentials(val error: SdkError) : Error
    data object ParseCredentials : Error
    data object UserToken : Error
    data object StartV2Ray : Error
  }

  companion object {
    const val TAG = "VPNConnector"
  }
}
