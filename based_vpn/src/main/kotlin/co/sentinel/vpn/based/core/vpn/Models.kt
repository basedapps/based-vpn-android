package co.sentinel.vpn.based.core.vpn

import androidx.annotation.StringRes
import co.sentinel.based_vpn.R

sealed interface Credentials {

  data class V2Ray(
    val payload: String,
    val uid: String,
  ) : Credentials

  data class Wireguard(
    val payload: String,
    val privateKey: String,
  ) : Credentials
}

enum class Protocol(
  val strValue: String,
  @StringRes val labelRes: Int,
) {
  WIREGUARD("WIREGUARD", R.string.settings_protocol_wireguard),
  V2RAY("V2RAY", R.string.settings_protocol_v2ray),
  NONE("NONE", R.string.settings_protocol_any),
  ;

  companion object {
    fun fromString(strValue: String) = entries.firstOrNull { it.strValue == strValue } ?: NONE
  }
}
