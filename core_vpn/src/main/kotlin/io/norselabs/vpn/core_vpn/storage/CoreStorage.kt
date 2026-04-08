package io.norselabs.vpn.core_vpn.storage

import android.content.SharedPreferences
import io.norselabs.vpn.common.preferences.delegate
import io.norselabs.vpn.core_vpn.vpn.Protocol
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CoreStorage(
  private val prefs: SharedPreferences,
) {

  private var tokenPref = prefs.delegate("device_token", "")

  private var deviceIdPref = prefs.delegate("user_id", "")

  private var protocolPref = prefs.delegate("selected_protocol", "")

  private var currentServerIdPref = prefs.delegate("last_server_id", "")

  private var alternativeRoutingPref = prefs.delegate("alternative_routing", false)

  fun setToken(token: String) {
    tokenPref.value = token
  }

  fun getToken(): String {
    return tokenPref.value
  }

  fun setDeviceId(deviceId: String) {
    deviceIdPref.value = deviceId
  }

  fun getDeviceId(): String {
    return deviceIdPref.value
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

  fun observeVpnProtocol(): Flow<Protocol?> {
    return protocolPref.observe.map(Protocol::fromString)
  }

  fun setCurrentServerId(serverId: String?) {
    currentServerIdPref.value = serverId.orEmpty()
  }

  fun getCurrentServerId(): String {
    return currentServerIdPref.value
  }

  fun observeCurrentServerId(): Flow<String> {
    return currentServerIdPref.observe
  }

  fun setAlternativeRouting(isEnabled: Boolean) {
    alternativeRoutingPref.value = isEnabled
  }

  fun isAlternativeRouting(): Boolean {
    return alternativeRoutingPref.value
  }

  fun observeAlternativeRouting(): Flow<Boolean> {
    return alternativeRoutingPref.observe
  }

  fun clearUserData() {
    tokenPref.clear()
    deviceIdPref.clear()
    protocolPref.clear()
    currentServerIdPref.clear()
    alternativeRoutingPref.clear()
  }
}
