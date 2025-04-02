package io.norselabs.vpn.based.viewModel.cities

import io.norselabs.vpn.common.state.Status
import io.norselabs.vpn.common.state.ViewStateHolder
import io.norselabs.vpn.sdk.services.destination.Country
import javax.inject.Inject
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

class CitiesScreenStateHolder
@Inject
constructor() : ViewStateHolder<CitiesScreenState, CitiesScreenEffect>(
  CitiesScreenState(),
)

data class CitiesScreenState(
  val status: Status = Status.Loading,
  val countryId: String? = null,
  val country: Country? = null,
  val cities: PersistentList<CityUi> = persistentListOf(),
)

data class CityUi(
  val id: String,
  val countryId: String,
  val name: String,
  val serversAvailable: Int,
)

sealed interface CitiesScreenEffect {
  data object GoBack : CitiesScreenEffect
  data object GoBackToRoot : CitiesScreenEffect
}
