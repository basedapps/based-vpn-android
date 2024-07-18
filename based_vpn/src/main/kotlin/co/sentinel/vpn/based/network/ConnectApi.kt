package co.sentinel.vpn.based.network

import co.sentinel.vpn.based.network.model.Credentials
import co.sentinel.vpn.based.network.model.DataObj
import retrofit2.http.POST
import retrofit2.http.Path

interface ConnectApi {

  /**
   * 400 — некорректный запрос
   * 500 — внутренняя ошибка сервера
   * 401 — не авторизован (нет токена или он неверный)
   * 410 — сервер, к которому попытались подключиться, умер и не отвечает
   * 425 – deviceNotEnrolled (кошелек создается)
   */
  @POST("countries/{countryId}/cities/{cityId}/credentials/{protocol}")
  suspend fun getCredentials(
    @Path("countryId") countryId: Int,
    @Path("cityId") cityId: Int,
    @Path("protocol") protocol: String,
  ): DataObj<Credentials>
}
