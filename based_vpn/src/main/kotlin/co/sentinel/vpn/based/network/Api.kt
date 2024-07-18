package co.sentinel.vpn.based.network

import co.sentinel.vpn.based.network.model.City
import co.sentinel.vpn.based.network.model.Country
import co.sentinel.vpn.based.network.model.DataList
import co.sentinel.vpn.based.network.model.DataObj
import co.sentinel.vpn.based.network.model.IpModel
import co.sentinel.vpn.based.network.model.TokenModel
import co.sentinel.vpn.based.network.model.VersionModel
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface Api {

  @POST("device")
  suspend fun registerDevice(@Body body: Map<String, String> = mapOf("platform" to "ANDROID")): DataObj<TokenModel>

  @GET("device")
  suspend fun getSession(): DataObj<TokenModel>

  @GET("countries")
  suspend fun getCountries(@Query("protocol") protocol: String?): DataList<Country>

  @GET("countries/{countryId}/cities")
  suspend fun getCities(
    @Path("countryId") countryId: Int,
    @Query("protocol") protocol: String?,
  ): DataList<City>

  @GET("ip")
  suspend fun getIp(): DataObj<IpModel>

  @GET("versions")
  suspend fun getVersion(): DataObj<VersionModel>
}
