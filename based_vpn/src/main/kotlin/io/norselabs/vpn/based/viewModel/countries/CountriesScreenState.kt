package io.norselabs.vpn.based.viewModel.countries

import io.norselabs.vpn.based.network.model.Country
import io.norselabs.vpn.common.state.Status
import io.norselabs.vpn.common.state.ViewStateHolder
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
