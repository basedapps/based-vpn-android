package co.uk.basedapps.vpn.network

import co.uk.basedapps.vpn.network.model.City
import co.uk.basedapps.vpn.network.model.Country
import co.uk.basedapps.vpn.network.model.Credentials
import co.uk.basedapps.vpn.network.model.DataList
import co.uk.basedapps.vpn.network.model.DataObj
import co.uk.basedapps.vpn.network.model.IpModel
import co.uk.basedapps.vpn.network.model.TokenModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient

class BasedRepository
@Inject constructor(
  private val api: Api,
  private val client: OkHttpClient,
) {

  suspend fun registerDevice(): NetResult<DataObj<TokenModel>> =
    execute(api::registerDevice)

  suspend fun getCountries(): NetResult<DataList<Country>> =
    execute(api::getCountries)

  suspend fun getCities(countryId: Int): NetResult<DataList<City>> =
    execute { api.getCities(countryId) }

  suspend fun getCredentials(
    countryId: Int,
    cityId: Int,
  ): NetResult<DataObj<Credentials>> = execute {
    api.getCredentials(
      countryId = countryId,
      cityId = cityId,
    )
  }

  suspend fun getIp(): NetResult<DataObj<IpModel>> =
    execute { api.getIp() }

  suspend fun resetConnection() {
    withContext(Dispatchers.IO) {
      client.connectionPool.evictAll()
    }
  }
}
