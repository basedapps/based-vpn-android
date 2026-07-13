package io.norselabs.vpn.core_vpn.vpn.connector

import io.norselabs.vpn.core_vpn.vpn.Destination
import io.norselabs.vpn.core_vpn.vpn.Protocol
import io.norselabs.vpn.v2ray.model.V2RayStartError
import java.util.UUID

@JvmInline
value class AttemptId(val value: String) {
  companion object {
    fun generate(): AttemptId = AttemptId(UUID.randomUUID().toString())
  }
}

sealed interface DisconnectReason {
  data object UserRequested : DisconnectReason
  data object HealthCheckFailed : DisconnectReason
  data object InvalidProfile : DisconnectReason
  data class TunnelEstablishFailed(val error: V2RayStartError) : DisconnectReason
  data class SdkInitiated(val cause: String) : DisconnectReason
  data object Unknown : DisconnectReason
}

sealed interface CredentialsError {
  data class ServerError(
    val httpCode: Int,
    val errorCode: String?,
    val message: String?,
  ) : CredentialsError

  data class Auth(
    val httpCode: Int,
    val errorCode: String?,
    val message: String?,
  ) : CredentialsError

  data class Other(val throwable: Throwable) : CredentialsError
}

data class ConnectStartedEvent(
  val attemptId: AttemptId,
  val destination: Destination,
  val protocol: Protocol?,
)

data class CredentialsReceivedEvent(
  val attemptId: AttemptId,
  val durationMs: Long,
  val serverId: String,
)

data class CredentialsFailedEvent(
  val attemptId: AttemptId,
  val durationMs: Long,
  val error: CredentialsError,
)

data class DisconnectEvent(
  val attemptId: AttemptId,
  val reason: DisconnectReason,
  val sessionDurationMs: Long,
)
