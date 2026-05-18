package io.norselabs.vpn.core_vpn.vpn.connector

import arrow.core.Either
import io.norselabs.vpn.v2ray.error.V2RayError
import io.norselabs.vpn.v2ray.model.VpnProfile

/**
 * Outbound interface that [VPNConnector] uses to drive the actual VPN tunnel
 * and refresh the DVPN HTTP client. Implementations wrap a v2ray backend
 * (or any other tunnel implementation) and the DVPN client.
 *
 * All methods are invoked synchronously on the dispatcher that called
 * [VPNConnector.connect] / [VPNConnector.disconnect]. Implementations must
 * not block; hand off heavy work to their own scopes.
 */
interface VPNDriver {

  /**
   * Starts the v2ray tunnel using [vpnProfile].
   *
   * Implementations may instrument tunnel-level events (e.g. `tunnel_started`,
   * `tunnel_up`, `tunnel_failed`) using [attemptId] — it identifies the same
   * logical attempt as the one delivered to
   * [ConnectionLifecycleListener.onConnectStarted], so consumers do not need
   * to maintain their own `currentAttemptId` field.
   *
   * @return `Right(Unit)` when the tunnel is up; `Left(error)` when start
   *         failed. The connector treats `Left` as a terminal failure and
   *         fires [ConnectionLifecycleListener.onDisconnect] with
   *         [DisconnectReason.TunnelEstablishFailed].
   */
  suspend fun startVpn(
    vpnProfile: VpnProfile,
    attemptId: AttemptId,
  ): Either<V2RayError, Unit>

  /**
   * Tears down the v2ray tunnel.
   *
   * Called by [VPNConnector.disconnect] **before** the analytics terminal
   * callback, so implementations see [reason] up front and can log it along
   * with the actual stop. Must be idempotent — may be invoked when no tunnel
   * is active.
   */
  fun stopVpn(reason: DisconnectReason)

  /**
   * Synchronous snapshot of the tunnel state. Used by the connector to detect
   * "connect while already connected" and emit an internal reconnect
   * disconnect first. Not a reactive source — consumers wanting to react to
   * tunnel-down should observe their own underlying state.
   */
  fun isVpnConnected(): Boolean

  /**
   * Refresh the DVPN HTTP client (e.g. drop the existing connection pool) so
   * subsequent traffic uses fresh sockets. The connector calls this after a
   * successful tunnel-up and after every disconnect.
   */
  fun resetNetworkClient()
}
