package co.uk.basedapps.domain_v2ray.core

import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow

interface V2RayUserPreferenceStore {

  val serverId: Flow<String?>

  suspend fun setServerId(serverId: String?): Preferences
}
