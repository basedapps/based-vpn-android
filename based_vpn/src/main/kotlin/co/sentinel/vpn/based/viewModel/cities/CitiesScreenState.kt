package co.sentinel.vpn.based.viewModel.cities

import co.sentinel.vpn.based.common.state.Status
import co.sentinel.vpn.based.common.state.ViewStateHolder
import co.sentinel.vpn.based.network.model.City
import co.sentinel.vpn.based.network.model.Country
import javax.inject.Inject

class CitiesScreenStateHolder
@Inject
constructor() : ViewStateHolder<CitiesScreenState, CitiesScreenEffect>(
  CitiesScreenState(),
)

data class CitiesScreenState(
  val status: Status = Status.Loading,
  val countryId: Int? = null,
  val country: Country? = null,
  val cities: List<City> = emptyList(),
)

sealed interface CitiesScreenEffect {
  object GoBackToRoot : CitiesScreenEffect
}
