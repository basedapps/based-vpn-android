package co.sentinel.vpn.based.network.repository

import co.sentinel.vpn.based.network.NetResult
import co.sentinel.vpn.based.network.model.City
import co.sentinel.vpn.based.network.model.Country
import co.sentinel.vpn.based.network.model.Credentials
import co.sentinel.vpn.based.network.model.DataList
import co.sentinel.vpn.based.network.model.DataObj
import co.sentinel.vpn.based.network.model.IpModel
import co.sentinel.vpn.based.network.model.Protocol
import co.sentinel.vpn.based.network.model.TokenModel
import co.sentinel.vpn.based.network.model.VersionModel

interface BasedRepository {

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
    countryId: Int,
    cityId: Int,
    protocol: Protocol?,
  ): NetResult<DataObj<Credentials>>

  suspend fun getIp(): NetResult<DataObj<IpModel>>
  suspend fun getVersion(): NetResult<DataObj<VersionModel>>
  suspend fun resetConnection()
}
