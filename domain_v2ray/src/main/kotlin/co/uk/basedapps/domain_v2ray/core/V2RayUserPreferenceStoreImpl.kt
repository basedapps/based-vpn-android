package co.uk.basedapps.domain_v2ray.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class V2RayUserPreferenceStoreImpl(private val context: Context) : V2RayUserPreferenceStore {
  private val Context.preferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings_v2ray")

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
}
