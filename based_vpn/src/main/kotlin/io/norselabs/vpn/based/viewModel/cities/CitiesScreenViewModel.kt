package io.norselabs.vpn.based.viewModel.cities

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.norselabs.vpn.based.viewModel.cities.CitiesScreenEffect as Effect
import io.norselabs.vpn.common.state.Status
import io.norselabs.vpn.core_vpn.storage.CoreStorage
import io.norselabs.vpn.core_vpn.vpn.Destination
import io.norselabs.vpn.core_vpn.vpn.destination.DestinationStorage
import io.norselabs.vpn.sdk.dvpn_client.DVPNClient
import io.norselabs.vpn.sdk.services.destination.api.CitiesRequest
import javax.inject.Inject
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch

class CitiesScreenViewModel
@Inject constructor(
  val stateHolder: CitiesScreenStateHolder,
  private val dvpnClient: DVPNClient,
  private val coreStorage: CoreStorage,
  private val destinationStorage: DestinationStorage,
) : ScreenModel {

  private val state: CitiesScreenState
    get() = stateHolder.state.value

  fun setCountryId(countryId: String) {
    stateHolder.updateState {
      copy(
        status = Status.Loading,
        countryId = countryId,
      )
    }
    getCities(countryId)
  }

  private fun getCities(countryId: String) {
    screenModelScope.launch {
      val protocol = coreStorage.getVpnProtocol()
      val countries = dvpnClient.getCountries(protocol?.strValue).getOrNull()
      val cities = dvpnClient.getCities(CitiesRequest(countryId, protocol?.strValue)).getOrNull()
      if (countries != null && cities != null) {
        stateHolder.updateState {
          copy(
            status = Status.Data,
            country = countries.first { it.id == countryId },
            cities = cities.map { city ->
              CityUi(city.id, city.countryId, city.name, city.serversAvailable)
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
    destinationStorage.storeDestination(
      Destination.Country(
        countryId = country.id,
        countryName = country.name,
        countryCode = country.code,
      ),
    )
    stateHolder.sendEffect(Effect.GoBackToRoot)
  }

  fun onCityClick(city: CityUi) {
    stateHolder.sendEffect(
      Effect.ShowServersScreen(
        countryId = city.countryId,
        cityId = city.id,
      ),
    )
  }

  fun onBackClick() {
    stateHolder.sendEffect(Effect.GoBack)
  }

  fun onTryAgainClick() {
    stateHolder.updateState {
      copy(status = Status.Error(true))
    }
    state.countryId?.let(::getCities)
  }
}
