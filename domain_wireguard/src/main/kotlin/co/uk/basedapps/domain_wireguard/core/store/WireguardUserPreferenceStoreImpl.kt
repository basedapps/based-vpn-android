package co.uk.basedapps.domain_wireguard.core.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import co.uk.basedapps.domain_wireguard.core.repo.WireguardRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WireguardUserPreferenceStoreImpl(private val context: Context) : WireguardUserPreferenceStore {
  private val Context.preferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

  private val DISABLE_KERNEL_MODULE = booleanPreferencesKey("disable_kernel_module")
  override val disableKernelModule: Flow<Boolean>
    get() = context.preferencesDataStore.data.map {
      it[DISABLE_KERNEL_MODULE] ?: false
    }

  private val MULTIPLE_TUNNELS = booleanPreferencesKey("multiple_tunnels")
  override val multipleTunnels: Flow<Boolean>
    get() = context.preferencesDataStore.data.map {
      it[MULTIPLE_TUNNELS] ?: false
    }

  private val ALLOW_REMOTE_CONTROL_INTENTS = booleanPreferencesKey("allow_remote_control_intents")
  override val allowRemoteControlIntents: Flow<Boolean>
    get() = context.preferencesDataStore.data.map {
      it[ALLOW_REMOTE_CONTROL_INTENTS] ?: false
    }

  private val RESTORE_ON_BOOT = booleanPreferencesKey("restore_on_boot")
  override val restoreOnBoot: Flow<Boolean>
    get() = context.preferencesDataStore.data.map {
      it[RESTORE_ON_BOOT] ?: false
    }

  private val LAST_USED_TUNNEL = stringPreferencesKey("last_used_tunnel")
  override val lastUsedTunnel: Flow<String?>
    get() = context.preferencesDataStore.data.map {
      it[LAST_USED_TUNNEL]
    }

  override suspend fun setLastUsedTunnel(lastUsedTunnel: String?) =
    context.preferencesDataStore.edit { prefs ->
      prefs.apply {
        if (lastUsedTunnel == null) {
          remove(LAST_USED_TUNNEL)
        } else {
          this[LAST_USED_TUNNEL] = lastUsedTunnel
        }
      }
    }

  private val RUNNING_TUNNELS = stringSetPreferencesKey("enabled_configs")
  override val runningTunnels: Flow<Set<String>>
    get() = context.preferencesDataStore.data.map {
      it[RUNNING_TUNNELS] ?: setOf()
    }

  override suspend fun setRunningTunnels(runningTunnels: Set<String>?) =
    context.preferencesDataStore.edit { prefs ->
      prefs.apply {
        if (runningTunnels == null) {
          remove(RUNNING_TUNNELS)
        } else {
          this[RUNNING_TUNNELS] = runningTunnels
        }
      }
    }

  private val SERVER_ID = stringPreferencesKey("server_id")
  override val serverId: Flow<String?>
    get() = context.preferencesDataStore.data.map {
      it[SERVER_ID]
    }

  override suspend fun setServerId(serverId: String?): Preferences =
    context.preferencesDataStore.edit { prefs ->
      prefs.apply {
        if (serverId == null) {
          remove(SERVER_ID)
        } else {
          this[SERVER_ID] = serverId
        }
      }
    }

  private val DNS = stringPreferencesKey("dns")
  override val dns: Flow<String>
    get() = context.preferencesDataStore.data.map {
      it[DNS] ?: WireguardRepositoryImpl.DEFAULT_DNS
    }

  override suspend fun setDns(dns: String?) =
    context.preferencesDataStore.edit { prefs ->
      prefs.apply {
        if (dns == null) {
          remove(DNS)
        } else {
          this[DNS] = dns
        }
      }
    }
}
