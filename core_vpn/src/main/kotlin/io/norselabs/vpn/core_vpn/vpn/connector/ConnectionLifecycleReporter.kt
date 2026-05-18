package io.norselabs.vpn.core_vpn.vpn.connector

import io.norselabs.vpn.core_vpn.vpn.Destination
import io.norselabs.vpn.core_vpn.vpn.Protocol
import io.norselabs.vpn.sdk.common.SdkError
import kotlinx.coroutines.CancellationException

/**
 * Tracks a single VPN connection attempt and dispatches the corresponding
 * analytics callbacks on [ConnectionLifecycleListener]. Owns the [AttemptId],
 * timestamps and phase flags; [VPNConnector] only tells it what happened.
 *
 * Lifecycle contract (per attempt) — see [ConnectionLifecycleListener] for
 * full details:
 *   start → credentialsReceived → (disconnect | cancelled)
 *   start → (credentialsFailed | cancelled)
 */
internal class ConnectionLifecycleReporter(
  private val listener: ConnectionLifecycleListener,
  private val clock: () -> Long,
) {

  @Volatile
  private var current: Attempt? = null

  fun start(destination: Destination, protocol: Protocol?): AttemptId {
    val attempt = Attempt(AttemptId.generate(), clock())
    current = attempt
    listener.onConnectStarted(ConnectStartedEvent(attempt.attemptId, destination, protocol))
    return attempt.attemptId
  }

  fun credentialsReceived(serverId: String?) {
    val attempt = current ?: return
    attempt.credentialsReceived = true
    listener.onCredentialsReceived(
      CredentialsReceivedEvent(
        attemptId = attempt.attemptId,
        durationMs = clock() - attempt.startedAtMs,
        serverId = serverId.orEmpty(),
      ),
    )
  }

  fun credentialsFailed(error: VPNConnector.Error) {
    emitCredentialsFailed(classify(error))
  }

  fun tunnelUp() {
    current?.tunnelUpAtMs = clock()
  }

  fun disconnect(reason: DisconnectReason) {
    val attempt = current ?: return
    if (attempt.credentialsReceived) {
      val sessionDurationMs = attempt.tunnelUpAtMs?.let { clock() - it } ?: 0L
      listener.onDisconnect(DisconnectEvent(attempt.attemptId, reason, sessionDurationMs))
      current = null
    } else {
      emitCredentialsFailed(
        CredentialsError.Other(IllegalStateException("disconnect before credentials")),
      )
    }
  }

  fun cancelled(ce: CancellationException) {
    val attempt = current ?: return
    if (!attempt.credentialsReceived) {
      emitCredentialsFailed(CredentialsError.Other(ce))
    } else {
      val sessionDurationMs = attempt.tunnelUpAtMs?.let { clock() - it } ?: 0L
      listener.onDisconnect(
        DisconnectEvent(attempt.attemptId, DisconnectReason.SdkInitiated("cancelled"), sessionDurationMs),
      )
      current = null
    }
  }

  private fun emitCredentialsFailed(error: CredentialsError) {
    val attempt = current ?: return
    listener.onCredentialsFailed(
      CredentialsFailedEvent(
        attemptId = attempt.attemptId,
        durationMs = clock() - attempt.startedAtMs,
        error = error,
      ),
    )
    current = null
  }

  private fun classify(error: VPNConnector.Error): CredentialsError = when (error) {
    is VPNConnector.Error.UserToken -> CredentialsError.Auth(
      httpCode = error.error.code,
      errorCode = error.error.error,
      message = error.error.reason,
    )
    is VPNConnector.Error.GetCredentials -> when (val sdkError = error.error) {
      is SdkError.HttpError -> CredentialsError.ServerError(
        httpCode = sdkError.code,
        errorCode = sdkError.error,
        message = sdkError.reason,
      )
      else -> CredentialsError.Other(RuntimeException(sdkError.toString()))
    }
    VPNConnector.Error.ParseCredentials -> CredentialsError.Other(IllegalStateException("ParseCredentials"))
    VPNConnector.Error.StartV2Ray -> CredentialsError.Other(IllegalStateException("StartV2Ray"))
  }

  private class Attempt(
    val attemptId: AttemptId,
    val startedAtMs: Long,
    @Volatile var credentialsReceived: Boolean = false,
    @Volatile var tunnelUpAtMs: Long? = null,
  )
}
