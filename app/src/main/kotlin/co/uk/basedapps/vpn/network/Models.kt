package co.uk.basedapps.vpn.network

import co.uk.basedapps.vpn.common.CountryFlag
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
  val isBanned: String,
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

enum class Protocol {
  WIREGUARD,
  V2RAY,
}
