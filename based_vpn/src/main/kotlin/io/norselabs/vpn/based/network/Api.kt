package io.norselabs.vpn.based.network

import io.norselabs.vpn.based.network.model.City
import io.norselabs.vpn.based.network.model.Country
import io.norselabs.vpn.based.network.model.DataList
import io.norselabs.vpn.based.network.model.DataObj
import io.norselabs.vpn.based.network.model.IpData
import io.norselabs.vpn.based.network.model.TokenModel
import io.norselabs.vpn.based.network.model.VersionModel
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
    @Path("countryId") countryId: String,
    @Query("protocol") protocol: String?,
  ): DataList<City>

  @GET("ip")
  suspend fun getIp(): DataObj<IpData>

  @GET("versions")
  suspend fun getVersion(): DataObj<VersionModel>
}
