package co.uk.basedapps.vpn.viewModel.cities

import co.uk.basedapps.vpn.common.state.Status
import co.uk.basedapps.vpn.common.state.ViewStateHolder
import co.uk.basedapps.vpn.network.model.City
import co.uk.basedapps.vpn.network.model.Country
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
