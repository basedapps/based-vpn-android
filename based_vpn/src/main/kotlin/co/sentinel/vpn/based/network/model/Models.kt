package co.sentinel.vpn.based.network.model

import com.google.gson.annotations.SerializedName
import io.norselabs.vpn.common_flags.CountryFlag
import io.norselabs.vpn.common_flags.mapToFlag
import io.norselabs.vpn.core_vpn.vpn.Protocol

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
  val id: String,
  @SerializedName("token")
  val token: String,
  @SerializedName("is_banned")
  val isBanned: Boolean,
  @SerializedName("is_enrolled")
  val isEnrolled: Boolean,
)

data class Country(
  @SerializedName("id")
  val id: String,
  @SerializedName("name")
  val name: String,
  @SerializedName("code")
  val code: String?,
  @SerializedName("servers_available")
  val serversAvailable: Int,
) {
  val flag: CountryFlag?
    get() = mapToFlag(code) // todo: bad solution
}

data class City(
  @SerializedName("id")
  val id: String,
  @SerializedName("country_id")
  val countryId: String,
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
  @SerializedName("server")
  val server: Server?,
)

data class IpData(
  @SerializedName("ip")
  val ip: String,
  @SerializedName("latitude")
  val latitude: Double,
  @SerializedName("longitude")
  val longitude: Double,
)

data class VersionModel(
  @SerializedName("ANDROID")
  val appVersion: Long,
  @SerializedName("API")
  val apiVersion: Long,
)

data class Server(
  @SerializedName("id")
  val id: String,
)
