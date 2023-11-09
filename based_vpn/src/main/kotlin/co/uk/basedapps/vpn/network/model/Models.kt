package co.uk.basedapps.vpn.network.model

import androidx.annotation.StringRes
import co.uk.basedapps.vpn.R
import co.uk.basedapps.vpn.common.flags.CountryFlag
import com.google.gson.annotations.SerializedName

data class DataObj<T>(
  @SerializedName("data")
  val data: T,
)

data class DataList<T>(
  @SerializedName("data")
  val data: List<T>,
)

data class TokenModel(
  @SerializedName("id")
  val id: Int,
  @SerializedName("token")
  val token: String,
  @SerializedName("is_banned")
  val isBanned: Boolean,
  @SerializedName("is_enrolled")
  val isEnrolled: Boolean,
)

data class Country(
  @SerializedName("id")
  val id: Int,
  @SerializedName("name")
  val name: String,
  @SerializedName("code")
  val flag: CountryFlag?,
  @SerializedName("servers_available")
  val serversAvailable: Int,
)

data class City(
  @SerializedName("id")
  val id: Int,
  @SerializedName("country_id")
  val countryId: Int,
  @SerializedName("name")
  val name: String,
  @SerializedName("servers_available")
  val serversAvailable: Int,
)

data class Credentials(
  @SerializedName("protocol")
  val protocol: Protocol,
  @SerializedName("payload")
  val payload: String,
  @SerializedName(value = "private_key", alternate = ["uid"])
  val privateKey: String,
)

data class IpModel(
  @SerializedName("ip")
  val ip: String,
  @SerializedName("latitude")
  val latitude: Double,
  @SerializedName("longitude")
  val longitude: Double,
)

enum class Protocol(
  val strValue: String,
  @StringRes val labelRes: Int,
) {
  WIREGUARD("WIREGUARD", R.string.settings_protocol_wireguard),
  V2RAY("V2RAY", R.string.settings_protocol_v2ray),
  NONE("NONE", R.string.settings_protocol_any),
  ;

  companion object {
    fun fromString(strValue: String) =
      values().firstOrNull { it.strValue == strValue } ?: NONE
  }
}
