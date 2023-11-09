package co.uk.basedapps.vpn.network.repository

import co.uk.basedapps.vpn.network.Api
import co.uk.basedapps.vpn.network.NetResult
import co.uk.basedapps.vpn.network.execute
import co.uk.basedapps.vpn.network.model.City
import co.uk.basedapps.vpn.network.model.Country
import co.uk.basedapps.vpn.network.model.Credentials
import co.uk.basedapps.vpn.network.model.DataList
import co.uk.basedapps.vpn.network.model.DataObj
import co.uk.basedapps.vpn.network.model.IpModel
import co.uk.basedapps.vpn.network.model.Protocol
import co.uk.basedapps.vpn.network.model.TokenModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient

class BasedRepositoryImpl(
  private val api: Api,
  private val client: OkHttpClient,
) : BasedRepository {

  override suspend fun registerDevice(): NetResult<DataObj<TokenModel>> =
    execute(api::registerDevice)

  override suspend fun getSession(): NetResult<DataObj<TokenModel>> =
    execute(api::getSession)

  override suspend fun getCountries(
    protocol: Protocol?,
  ): NetResult<DataList<Country>> = execute {
    api.getCountries(
      protocol = protocol?.strValue,
    )
  }

  override suspend fun getCities(
    countryId: Int,
    protocol: Protocol?,
  ): NetResult<DataList<City>> = execute {
    api.getCities(
      countryId = countryId,
      protocol = protocol?.strValue,
    )
  }

  override suspend fun getCredentials(
    countryId: Int,
    cityId: Int,
    protocol: Protocol?,
  ): NetResult<DataObj<Credentials>> = execute {
    api.getCredentials(
      countryId = countryId,
      cityId = cityId,
      protocol = protocol?.strValue ?: "",
    )
  }

  override suspend fun getIp(): NetResult<DataObj<IpModel>> =
    execute { api.getIp() }

  override suspend fun resetConnection() {
    withContext(Dispatchers.IO) {
      client.connectionPool.evictAll()
    }
  }
}
