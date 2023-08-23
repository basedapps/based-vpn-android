package co.uk.basedapps.vpn.network

import co.uk.basedapps.vpn.network.model.City
import co.uk.basedapps.vpn.network.model.Country
import co.uk.basedapps.vpn.network.model.Credentials
import co.uk.basedapps.vpn.network.model.DataList
import co.uk.basedapps.vpn.network.model.DataObj
import co.uk.basedapps.vpn.network.model.IpModel
import co.uk.basedapps.vpn.network.model.TokenModel
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface Api {

  @POST("device")
  suspend fun registerDevice(
    @Body body: Map<String, String> = mapOf("platform" to "ANDROID"),
  ): DataObj<TokenModel>

  @GET("countries")
  suspend fun getCountries(): DataList<Country>

  @GET("countries/{countryId}/cities")
  suspend fun getCities(
    @Path("countryId") countryId: Int,
  ): DataList<City>

  /**
   * 400 — некорректный запрос
   * 500 — внутренняя ошибка сервера
   * 401 — не авторизован (нет токена или он неверный)
   * 410 — сервер, к которому попытались подключиться, умер и не отвечает
   */
  @POST("countries/{countryId}/cities/{cityId}/credentials")
  suspend fun getCredentials(
    @Path("countryId") countryId: Int,
    @Path("cityId") cityId: Int,
  ): DataObj<Credentials>

  @GET("ip")
  suspend fun getIp(): DataObj<IpModel>
}
