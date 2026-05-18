package io.norselabs.vpn.core_vpn.vpn.connector

/**
 * Inbound callback interface — [VPNConnector] notifies the listener about
 * lifecycle events of a connection attempt. All methods have no-op defaults,
 * so consumers override only what they care about.
 *
 * ### Lifecycle contract (per [AttemptId])
 *
 * For every [AttemptId] produced by [VPNConnector.connect]:
 *
 * 1. [onConnectStarted] is invoked exactly once.
 * 2. Then exactly one of two terminal branches happens:
 *    - [onCredentialsFailed] (terminal), **or**
 *    - [onCredentialsReceived] followed by exactly one [onDisconnect] (terminal).
 *
 * No attempt is left "dangling" without a terminal callback.
 *
 * ### Dispatcher
 *
 * Callbacks are invoked synchronously on the dispatcher that called
 * [VPNConnector.connect] / [VPNConnector.disconnect]. Implementations must
 * not block; hand off heavy work to their own scopes.
 *
 * ### Cancellation
 *
 * If the coroutine running [VPNConnector.connect] is cancelled **before**
 * [onCredentialsReceived], the connector emits [onCredentialsFailed] with
 * [CredentialsError.Other] wrapping the `CancellationException`, then rethrows.
 *
 * ### Auto-reconnect
 *
 * If the SDK reconnects on its own (e.g. on a network change), that is a
 * **new** [AttemptId] — not a continuation of the previous one.
 *
 * ### Session duration
 *
 * [DisconnectEvent.sessionDurationMs] is measured from the moment
 * [VPNDriver.startVpn] returned `Right` to the moment [onDisconnect] fires.
 * If the tunnel never came up, the value is `0`.
 */
interface ConnectionLifecycleListener {

  /** Called as the first step of [VPNConnector.connect], once per attempt. */
  fun onConnectStarted(event: ConnectStartedEvent) {}

  /** Called once when credentials have been fetched successfully, before [VPNDriver.startVpn]. */
  fun onCredentialsReceived(event: CredentialsReceivedEvent) {}

  /** Terminal for attempts that failed before credentials were received. */
  fun onCredentialsFailed(event: CredentialsFailedEvent) {}

  /** Terminal for attempts that successfully reached [onCredentialsReceived]. */
  fun onDisconnect(event: DisconnectEvent) {}
}
