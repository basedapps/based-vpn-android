package io.norselabs.vpn.core_vpn.connectivity

import io.norselabs.vpn.core_vpn.user.UserInitializer
import io.norselabs.vpn.sdk.dvpn_client.DVPNClient
import io.norselabs.vpn.sdk.services.connection.api.NetworkData
import io.norselabs.vpn.v2ray.model.VpnConnection
import io.norselabs.vpn.v2ray.repo.V2RayRepository
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Keeps the externally visible IP/location ([networkData]) up to date.
 *
 * Owns the decision of *when* to re-fetch `getIpData()`: on every observation
 * of "network is up + tunnel in a stable state". While the tunnel is being
 * established (`Connecting`/`CoreStarted`) the request would still be routed
 * directly and report the real IP instead of the node's, so those states are
 * filtered out — the emission on reaching `Connected` refreshes instead.
 */
class NetworkInfoUpdater(
  scope: CoroutineScope,
  private val dvpn: DVPNClient,
  private val vpnRepo: V2RayRepository,
  private val networkMonitor: NetworkStateMonitor,
  private val userInitializer: UserInitializer,
) {

  private val _networkData = MutableStateFlow<NetworkData?>(null)
  val networkData: StateFlow<NetworkData?>
    get() = _networkData

  init {
    scope.launch {
      // No distinctUntilChanged: equal-looking network emissions (e.g. a
      // wi-fi → cellular switch) still mean a new IP. collectLatest: a new
      // trigger cancels a pending retry loop.
      combine(networkMonitor.networkState, vpnRepo.connectionState, ::Pair)
        .filter { (network, tunnel) ->
          network is NetworkState.Connected &&
            (tunnel is VpnConnection.Connected || tunnel is VpnConnection.Disconnected)
        }
        .collectLatest { fetchWithRetry() }
    }
  }

  // The first answer is trusted: routes are fresh by the time a trigger fires —
  // the connector drops the connection pool on tunnel-up/disconnect, and the
  // tunnel-bound client has no keep-alive at all. Retry only covers failures
  // (e.g. a transient flap while the network is switching).
  private suspend fun fetchWithRetry() {
    userInitializer.waitForDeviceToken()
    repeat(MAX_ATTEMPTS) { attempt ->
      val data = dvpn.getIpData().getOrNull()
      if (data != null) {
        _networkData.value = data
        return
      }
      Timber.tag(TAG).d("Network info request failed. Attempt ${attempt + 1}")
      delay(RETRY_DELAY_SEC.seconds)
    }
    Timber.tag(TAG).d("Network info refresh gave up after $MAX_ATTEMPTS attempts")
  }

  companion object {
    const val TAG = "NetworkInfoUpdater"
    const val MAX_ATTEMPTS = 3
    const val RETRY_DELAY_SEC = 1
  }
}
