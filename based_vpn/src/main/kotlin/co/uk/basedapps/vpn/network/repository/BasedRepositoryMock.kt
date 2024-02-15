package co.uk.basedapps.vpn.network.repository

import co.uk.basedapps.domain.functional.Either
import co.uk.basedapps.vpn.common.flags.CountryFlag
import co.uk.basedapps.vpn.network.NetResult
import co.uk.basedapps.vpn.network.model.City
import co.uk.basedapps.vpn.network.model.Country
import co.uk.basedapps.vpn.network.model.Credentials
import co.uk.basedapps.vpn.network.model.DataList
import co.uk.basedapps.vpn.network.model.DataObj
import co.uk.basedapps.vpn.network.model.IpModel
import co.uk.basedapps.vpn.network.model.Protocol
import co.uk.basedapps.vpn.network.model.TokenModel
import co.uk.basedapps.vpn.network.model.VersionModel

class BasedRepositoryMock : BasedRepository {

  override suspend fun registerDevice() = dataObj(
    TokenModel(
      id = 999,
      token = "HelloWorld",
      isBanned = false,
      isEnrolled = true,
    ),
  )

  override suspend fun getSession() = dataObj(
    TokenModel(
      id = 999,
      token = "HelloWorld",
      isBanned = false,
      isEnrolled = true,
    ),
  )

  override suspend fun getCountries(
    protocol: Protocol?,
  ) = dataList(
    listOf(
      Country(
        id = 1,
        name = "Argentina",
        flag = CountryFlag.AR,
        serversAvailable = 10,
      ),
      Country(
        id = 2,
        name = "Hungary",
        flag = CountryFlag.HU,
        serversAvailable = 5,
      ),
      Country(
        id = 3,
        name = "Georgia",
        flag = CountryFlag.GE,
        serversAvailable = 100,
      ),
    ),
  )

  override suspend fun getCities(
    countryId: Int,
    protocol: Protocol?,
  ) = dataList(
    listOf(
      City(
        id = 1,
        countryId = countryId,
        name = "Buenos Aires",
        serversAvailable = 10,
      ),
      City(
        id = 2,
        countryId = countryId,
        name = "Cordoba",
        serversAvailable = 10,
      ),
      City(
        id = 3,
        countryId = countryId,
        name = "Saint-Petersburg",
        serversAvailable = 10,
      ),
    ),
  )

  override suspend fun getCredentials(
    countryId: Int,
    cityId: Int,
    protocol: Protocol?,
  ) = dataObj(
    Credentials(
      protocol = Protocol.NONE,
      payload = "Hello",
      privateKey = "World",
    ),
  )

  override suspend fun getIp() = dataObj(
    IpModel(
      ip = "192.168.0.1",
      latitude = 0.0,
      longitude = 0.0,
    ),
  )

  override suspend fun getVersion() = dataObj(
    VersionModel(
      appVersion = 0,
      apiVersion = 0,
    ),
  )

  override suspend fun resetConnection() = Unit

  private fun <T> dataObj(data: T) = Either.Right(DataObj(data))
  private fun <T> dataList(data: List<T>) = Either.Right(DataList(data))
}
