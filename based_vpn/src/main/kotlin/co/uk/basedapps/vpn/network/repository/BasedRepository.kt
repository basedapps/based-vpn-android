package co.uk.basedapps.vpn.network.repository

import co.uk.basedapps.vpn.network.NetResult
import co.uk.basedapps.vpn.network.model.City
import co.uk.basedapps.vpn.network.model.Country
import co.uk.basedapps.vpn.network.model.Credentials
import co.uk.basedapps.vpn.network.model.DataList
import co.uk.basedapps.vpn.network.model.DataObj
import co.uk.basedapps.vpn.network.model.IpModel
import co.uk.basedapps.vpn.network.model.Protocol
import co.uk.basedapps.vpn.network.model.TokenModel

interface BasedRepository {

  suspend fun registerDevice(): NetResult<DataObj<TokenModel>>
  suspend fun getSession(): NetResult<DataObj<TokenModel>>
  suspend fun getCountries(protocol: Protocol?): NetResult<DataList<Country>>
  suspend fun getCities(countryId: Int, protocol: Protocol?): NetResult<DataList<City>>
  suspend fun getCredentials(countryId: Int, cityId: Int, protocol: Protocol?): NetResult<DataObj<Credentials>>
  suspend fun getIp(): NetResult<DataObj<IpModel>>
  suspend fun resetConnection()
}
