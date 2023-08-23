package co.uk.basedapps.vpn.ui.screens.countries

import co.uk.basedapps.vpn.common.Status
import co.uk.basedapps.vpn.common.ViewStateHolder
import co.uk.basedapps.vpn.network.model.Country
import javax.inject.Inject

class CountriesScreenStateHolder
@Inject
constructor() : ViewStateHolder<CountriesScreenState, CountriesScreenEffect>(
  CountriesScreenState(),
)

data class CountriesScreenState(
  val status: Status = Status.Data,
  val countries: List<Country> = emptyList(),
)

sealed interface CountriesScreenEffect {
  data class ShowCitiesScreen(val country: Country) : CountriesScreenEffect
}
