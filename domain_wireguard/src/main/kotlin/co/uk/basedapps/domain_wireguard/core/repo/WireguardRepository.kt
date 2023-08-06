package co.uk.basedapps.domain_wireguard.core.repo

import co.uk.basedapps.domain.functional.Either
import co.uk.basedapps.domain.models.KeyPair
import co.uk.basedapps.domain.models.VpnTunnel
import co.uk.basedapps.domain_wireguard.core.init.DefaultTunnelName
import co.uk.basedapps.domain_wireguard.core.model.TunnelConfig
import co.uk.basedapps.domain_wireguard.core.model.WireguardTunnel
import co.uk.basedapps.domain_wireguard.core.model.WireguardVpnProfile

/**
 * Repository that handles communication to the vpn implementation.
 */
interface WireguardRepository {

  /**
   * Inits and configures the backend. Must be called in the Application class before everything.
   */
  suspend fun init(alwaysOnCallback: () -> Unit)

  /**
   * Returns currently loaded tunnels, or loads and returns if called the first time.
   */
  suspend fun getTunnels(): List<WireguardTunnel>?

  /**
   * Creates a new configured tunnel.
   */
  suspend fun create(name: String, tunnelConfig: TunnelConfig): Either<Exception?, WireguardTunnel>

  /**
   * Deletes an existing tunnel.
   */
  suspend fun delete(name: String): Either<Unit, Unit>

  suspend fun isConnected(): Boolean

  /**
   * Fetches the tunnel configuration.
   */
  suspend fun getTunnelConfig(tunnelName: String): Either<Exception?, TunnelConfig>

  /**
   * Loads the tunnel list into cache.
   */
  suspend fun loadTunnels(): Boolean

  /**
   * Reloads tunnel states and updates the cached list.
   */
  fun refreshTunnelStates()

  /**
   * Helps restore tunnel config state when restarting app.
   */
  suspend fun restoreState(isForce: Boolean)

  /**
   * Saves current state of currently running tunnel so it can be restored later.
   */
  suspend fun saveState()

  /**
   * Sets new config parameters to a tunnel.
   */
  suspend fun setTunnelConfig(tunnelName: String, tunnelConfig: TunnelConfig): Either<Unit, Unit>

  /**
   * Updates tunnel name.
   */
  suspend fun setTunnelName(newTunnelName: String, wgTunnel: WireguardTunnel): Either<Exception?, Unit>

  /**
   * Updates tunnel state.
   */
  suspend fun setTunnelState(tunnelName: String, tunnelState: VpnTunnel.State): Either<Unit, WireguardTunnel>

  /**
   * Returns tunnel stats.
   */
  suspend fun getTunnelStatistics(tunnelName: String): Either<Exception?, Unit>

  /**
   * Generates private/public key pair for creating new tunnels.
   */
  fun generateKeyPair(): KeyPair

  fun generateKeyPair(privateKey: String): KeyPair

  suspend fun createOrUpdate(
    name: String = DefaultTunnelName,
    vpnProfile: WireguardVpnProfile,
    keyPair: KeyPair,
    serverId: String,
  ): Either<Exception?, WireguardTunnel>

  suspend fun getTunnel(
    tunnelName: String = DefaultTunnelName,
  ): VpnTunnel?

  suspend fun updateDns(dns: String): Either<Unit, Unit>
  suspend fun getDefaultDns(): String
}
