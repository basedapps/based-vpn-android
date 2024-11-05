package co.sentinel.vpn.based.network.repository

import co.sentinel.vpn.based.network.Api
import co.sentinel.vpn.based.network.ConnectApi
import co.sentinel.vpn.based.network.model.City
import co.sentinel.vpn.based.network.model.Country
import co.sentinel.vpn.based.network.model.Credentials
import co.sentinel.vpn.based.network.model.DataList
import co.sentinel.vpn.based.network.model.DataObj
import co.sentinel.vpn.based.network.model.IpData
import co.sentinel.vpn.based.network.model.TokenModel
import co.sentinel.vpn.based.network.model.VersionModel
import io.norselabs.vpn.common.network.NetResult
import io.norselabs.vpn.common.network.cache.CacheUnit
import io.norselabs.vpn.common.network.execute
import io.norselabs.vpn.core_vpn.vpn.Protocol
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient

class AppRepositoryImpl(
  private val api: Api,
  private val connectApi: ConnectApi,
  private val client: OkHttpClient,
) : AppRepository {

  private val countriesCache =
    CacheUnit<Protocol?, DataList<Country>> { protocol ->
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
    countryId: String,
    cityId: String,
    protocol: Protocol?,
  ): NetResult<DataObj<Credentials>> = execute {
    connectApi.getCredentials(
      countryId = countryId,
      cityId = cityId,
      protocol = protocol?.strValue ?: "",
    )
  }

  override suspend fun getIp(): NetResult<DataObj<IpData>> = execute { api.getIp() }

  override suspend fun getVersion(): NetResult<DataObj<VersionModel>> = execute { api.getVersion() }

  override suspend fun resetConnection() {
    withContext(Dispatchers.IO) {
      client.connectionPool.evictAll()
    }
  }
}

data class CitiesRequest(
  val countryId: String,
  val protocol: Protocol?,
)
