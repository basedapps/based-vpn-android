package co.uk.basedapps.vpn.storage

import co.uk.basedapps.vpn.common.flags.CountryFlag
import com.google.gson.annotations.SerializedName

data class SelectedCity(
  @SerializedName("id")
  val id: Int,
  @SerializedName("name")
  val name: String,
  @SerializedName("countryId")
  val countryId: Int,
  @SerializedName("countryName")
  val countryName: String,
  @SerializedName("countryFlag")
  val countryFlag: CountryFlag?,
) {

  val serverId: String = "$countryId:$id"
}

data class LogEntity(
  val time: Long,
  val message: String,
)
