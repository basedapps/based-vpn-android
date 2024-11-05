package co.sentinel.vpn.based.viewModel.cities

import co.sentinel.vpn.based.network.model.City
import co.sentinel.vpn.based.network.model.Country
import io.norselabs.vpn.common.state.Status
import io.norselabs.vpn.common.state.ViewStateHolder
import javax.inject.Inject

class CitiesScreenStateHolder
@Inject
constructor() : ViewStateHolder<CitiesScreenState, CitiesScreenEffect>(
  CitiesScreenState(),
)

data class CitiesScreenState(
  val status: Status = Status.Loading,
  val countryId: String? = null,
  val country: Country? = null,
  val cities: List<City> = emptyList(),
)

sealed interface CitiesScreenEffect {
  data object GoBackToRoot : CitiesScreenEffect
}
