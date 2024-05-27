package co.sentinel.vpn.based.network.repository

import co.sentinel.vpn.based.network.Api
import co.sentinel.vpn.based.network.NetResult
import co.sentinel.vpn.based.network.cache.CacheUnit
import co.sentinel.vpn.based.network.execute
import co.sentinel.vpn.based.network.model.City
import co.sentinel.vpn.based.network.model.Country
import co.sentinel.vpn.based.network.model.Credentials
import co.sentinel.vpn.based.network.model.DataList
import co.sentinel.vpn.based.network.model.DataObj
import co.sentinel.vpn.based.network.model.IpModel
import co.sentinel.vpn.based.network.model.Protocol
import co.sentinel.vpn.based.network.model.TokenModel
import co.sentinel.vpn.based.network.model.VersionModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient

class BasedRepositoryImpl(
  private val api: Api,
  private val client: OkHttpClient,
) : BasedRepository {

  private val countriesCache = CacheUnit<Protocol?, DataList<Country>> { protocol ->
    api.getCountries(protocol?.strValue)
  }

  private val citiesCache = CacheUnit<CitiesRequest, DataList<City>> { request ->
    api.getCities(request.countryId, request.protocol?.strValue)
  }

  override suspend fun registerDevice(): NetResult<DataObj<TokenModel>> = execute(api::registerDevice)

  override suspend fun getSession(): NetResult<DataObj<TokenModel>> = execute(api::getSession)

  override suspend fun getCountries(
    protocol: Protocol?,
    isFresh: Boolean,
  ): NetResult<DataList<Country>> = execute { countriesCache.get(protocol, isFresh) }

  override suspend fun getCities(
    request: CitiesRequest,
    isFresh: Boolean,
  ): NetResult<DataList<City>> = execute { citiesCache.get(request, isFresh) }

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

  override suspend fun getIp(): NetResult<DataObj<IpModel>> = execute { api.getIp() }

  override suspend fun getVersion(): NetResult<DataObj<VersionModel>> = execute { api.getVersion() }

  override suspend fun resetConnection() {
    withContext(Dispatchers.IO) {
      client.connectionPool.evictAll()
    }
  }
}

data class CitiesRequest(
  val countryId: Int,
  val protocol: Protocol?,
)
