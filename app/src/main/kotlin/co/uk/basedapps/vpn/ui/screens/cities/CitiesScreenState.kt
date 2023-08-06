package co.uk.basedapps.vpn.ui.screens.cities

import co.uk.basedapps.vpn.common.Status
import co.uk.basedapps.vpn.common.ViewStateHolder
import co.uk.basedapps.vpn.network.City
import co.uk.basedapps.vpn.network.Country
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
