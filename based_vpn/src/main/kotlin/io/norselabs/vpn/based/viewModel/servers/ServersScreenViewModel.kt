package io.norselabs.vpn.based.viewModel.servers

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.norselabs.vpn.based.viewModel.servers.ServersScreenEffect as Effect
import io.norselabs.vpn.common.state.Status
import io.norselabs.vpn.core_vpn.storage.CoreStorage
import io.norselabs.vpn.core_vpn.vpn.Destination
import io.norselabs.vpn.core_vpn.vpn.destination.DestinationStorage
import io.norselabs.vpn.sdk.dvpn_client.DVPNClient
import io.norselabs.vpn.sdk.services.destination.CitiesRequest
import io.norselabs.vpn.sdk.services.destination.ServersRequest
import javax.inject.Inject
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch

class ServersScreenViewModel
@Inject constructor(
  val stateHolder: ServersScreenStateHolder,
  private val dvpnClient: DVPNClient,
  private val coreStorage: CoreStorage,
  private val destinationStorage: DestinationStorage,
) : ScreenModel {

  private val state: ServersScreenState
    get() = stateHolder.state.value

  fun setData(countryId: String, cityId: String) {
    stateHolder.updateState {
      copy(
        status = Status.Loading,
        countryId = countryId,
        cityId = cityId,
      )
    }
    getServers(countryId, cityId)
  }

  private fun getServers(countryId: String, cityId: String) {
    screenModelScope.launch {
      val protocol = coreStorage.getVpnProtocol()?.strValue

      val country = dvpnClient.getCountries(
        protocol = protocol,
      ).getOrNull()?.firstOrNull { it.id == countryId }

      val city = dvpnClient.getCities(
        request = CitiesRequest(
          countryId = countryId,
          protocol = protocol,
        ),
      ).getOrNull()?.firstOrNull { it.id == cityId }

      val servers = dvpnClient.getServers(
        request = ServersRequest(
          cityId = cityId,
          protocol = protocol,
          isSortedByLoad = true,
        ),
      ).getOrNull()

      if (country != null && city != null && servers != null) {
        stateHolder.updateState {
          copy(
            status = Status.Data,
            country = country,
            city = city,
            servers = servers.map { city ->
              ServerUi(id = city.id, cityId = city.cityId, name = city.name)
            }.toPersistentList(),
          )
        }
      } else {
        stateHolder.updateState { copy(status = Status.Error(false)) }
      }
    }
  }

  fun onQuickConnectClick() {
    val country = state.country ?: return
    val city = state.city ?: return
    destinationStorage.storeDestination(
      Destination.City(
        cityId = city.id,
        cityName = city.name,
        countryId = country.id,
        countryName = country.name,
        countryCode = country.code,
      ),
    )
    stateHolder.sendEffect(Effect.GoBackToRoot)
  }

  fun onServerClick(server: ServerUi) {
    val country = state.country ?: return
    val city = state.city ?: return
    destinationStorage.storeDestination(
      Destination.Server(
        serverId = server.id,
        serverName = server.name,
        cityId = city.id,
        cityName = city.name,
        countryId = country.id,
        countryName = country.name,
        countryCode = country.code,
      ),
    )
    stateHolder.sendEffect(Effect.GoBackToRoot)
  }

  fun onBackClick() {
    stateHolder.sendEffect(Effect.GoBack)
  }

  fun onTryAgainClick() {
    stateHolder.updateState {
      copy(status = Status.Error(true))
    }
    val countryId = state.countryId
    val cityId = state.cityId
    if (countryId != null && cityId != null) {
      getServers(countryId = countryId, cityId = cityId)
    }
  }
}
