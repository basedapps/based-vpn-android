package io.norselabs.vpn.core_vpn.vpn

import com.google.gson.annotations.SerializedName
import io.norselabs.vpn.core_vpn.vpn.Protocol.entries

sealed interface Credentials {

  data class V2Ray(
    val payload: String,
    val uid: String,
    override val serverId: String?,
  ) : Credentials

  data class Wireguard(
    val payload: String,
    val privateKey: String,
    override val serverId: String?,
  ) : Credentials

  val serverId: String?
}

enum class Protocol(val strValue: String) {
  WIREGUARD("WIREGUARD"),
  V2RAY("V2RAY"),
  ;

  companion object {
    fun fromString(strValue: String): Protocol? {
      return entries.firstOrNull {
        it.strValue.equals(other = strValue, ignoreCase = true)
      }
    }
  }
}

sealed interface Destination {

  data class City(
    @SerializedName("cityId")
    val cityId: String,
    @SerializedName("cityName")
    val cityName: String,
    @SerializedName("countryId")
    val countryId: String,
    @SerializedName("countryName")
    val countryName: String,
    @SerializedName("countryCode")
    val countryCode: String?,
  ) : Destination {
    override val isPersistable: Boolean = true
  }

  data class Server(
    @SerializedName("serverId")
    val serverId: String,
    @SerializedName("serverName")
    val serverName: String,
    @SerializedName("cityId")
    val cityId: String,
    @SerializedName("cityName")
    val cityName: String,
    @SerializedName("countryId")
    val countryId: String,
    @SerializedName("countryName")
    val countryName: String,
    @SerializedName("countryCode")
    val countryCode: String,
  ) : Destination {
    override val isPersistable: Boolean = true
  }

  data class Deeplink(
    val url: String,
  ) : Destination {
    override val isPersistable: Boolean = false
  }

  val isPersistable: Boolean
}
