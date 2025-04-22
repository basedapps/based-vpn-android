package io.norselabs.vpn.based.viewModel.countries

import io.norselabs.vpn.common.state.Status
import io.norselabs.vpn.common.state.ViewStateHolder
import io.norselabs.vpn.common_flags.CountryFlag
import javax.inject.Inject
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

class CountriesScreenStateHolder
@Inject
constructor() : ViewStateHolder<CountriesScreenState, CountriesScreenEffect>(
  CountriesScreenState(),
)

data class CountriesScreenState(
  val status: Status = Status.Loading,
  val countries: PersistentList<CountryUi> = persistentListOf(),
  val isSubscribed: Boolean = false,
  val isRefreshing: Boolean = false,
)

data class CountryUi(
  val id: String,
  val name: String,
  val code: String,
  val flag: CountryFlag?,
  val serversAvailable: Int,
)

sealed interface CountriesScreenEffect {
  data object GoBack : CountriesScreenEffect
  data class ShowCitiesScreen(val countryId: String) : CountriesScreenEffect
  data object ShowPurchaseScreen : CountriesScreenEffect
}
