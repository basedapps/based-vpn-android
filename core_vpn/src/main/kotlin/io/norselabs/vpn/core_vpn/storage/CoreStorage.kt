package io.norselabs.vpn.core_vpn.storage

import android.content.SharedPreferences
import io.norselabs.vpn.common.preferences.delegate
import io.norselabs.vpn.core_vpn.vpn.Protocol
import kotlinx.coroutines.flow.Flow

class CoreStorage(
  private val prefs: SharedPreferences,
) {

  private var tokenPref = prefs.delegate("device_token", "")

  private var userIdPref = prefs.delegate("user_id", "")

  private var protocolPref = prefs.delegate("selected_protocol", "")

  private var currentServerIdPref = prefs.delegate("last_server_id", "")

  fun setToken(token: String) {
    tokenPref.value = token
  }

  fun getToken(): String {
    return tokenPref.value
  }

  fun setUserId(userId: String) {
    userIdPref.value = userId
  }

  fun getUserId(): String {
    return userIdPref.value
  }

  fun wasVpnProtocolSelected(): Boolean {
    return prefs.getString("selected_protocol", null) != null
  }

  fun setVpnProtocol(protocol: Protocol?) {
    protocolPref.value = protocol?.strValue.orEmpty()
  }

  fun getVpnProtocol(): Protocol? {
    return Protocol.fromString(protocolPref.value)
  }

  fun setCurrentServerId(serverId: String?) {
    currentServerIdPref.value = serverId.orEmpty()
  }

  fun getCurrentServerId(): String = currentServerIdPref.value

  fun observeCurrentServerId(): Flow<String> = currentServerIdPref.observe

  fun clearUserData() {
    tokenPref.clear()
    userIdPref.clear()
    protocolPref.clear()
    currentServerIdPref.clear()
  }
}
