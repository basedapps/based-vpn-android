package co.sentinel.vpn.based.viewModel.countries

import co.sentinel.vpn.based.network.model.Country
import co.sentinel.vpn.based.state.Status
import co.sentinel.vpn.based.state.ViewStateHolder
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
