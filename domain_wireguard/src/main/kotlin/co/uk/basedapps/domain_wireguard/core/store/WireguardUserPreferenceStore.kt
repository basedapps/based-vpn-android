package co.uk.basedapps.domain_wireguard.core.store

import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow

interface WireguardUserPreferenceStore {

  val disableKernelModule: Flow<Boolean>

  val multipleTunnels: Flow<Boolean>

  val allowRemoteControlIntents: Flow<Boolean>

  val restoreOnBoot: Flow<Boolean>

  val lastUsedTunnel: Flow<String?>
  suspend fun setLastUsedTunnel(lastUsedTunnel: String?): Preferences

  val runningTunnels: Flow<Set<String>>
  suspend fun setRunningTunnels(runningTunnels: Set<String>?): Preferences

  val serverId: Flow<String?>

  suspend fun setServerId(serverId: String?): Preferences

  val dns: Flow<String>
  suspend fun setDns(dns: String?): Preferences
}
