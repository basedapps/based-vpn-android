package io.norselabs.vpn.based.network.repository

import io.norselabs.vpn.based.network.model.City
import io.norselabs.vpn.based.network.model.Country
import io.norselabs.vpn.based.network.model.Credentials
import io.norselabs.vpn.based.network.model.DataList
import io.norselabs.vpn.based.network.model.DataObj
import io.norselabs.vpn.based.network.model.IpData
import io.norselabs.vpn.based.network.model.TokenModel
import io.norselabs.vpn.based.network.model.VersionModel
import io.norselabs.vpn.common.network.NetResult
import io.norselabs.vpn.core_vpn.vpn.Protocol

interface AppRepository {

  suspend fun registerDevice(): NetResult<DataObj<TokenModel>>
  suspend fun getSession(): NetResult<DataObj<TokenModel>>
  suspend fun getCountries(
    protocol: Protocol?,
    isFresh: Boolean = false,
  ): NetResult<DataList<Country>>

  suspend fun getCities(
    request: CitiesRequest,
    isFresh: Boolean = false,
  ): NetResult<DataList<City>>

  suspend fun getCredentials(
    countryId: String,
    cityId: String,
    protocol: Protocol?,
  ): NetResult<DataObj<Credentials>>

  suspend fun getIp(): NetResult<DataObj<IpData>>
  suspend fun getVersion(): NetResult<DataObj<VersionModel>>
  suspend fun resetConnection()
}
