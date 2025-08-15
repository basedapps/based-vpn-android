package io.norselabs.vpn.based.viewModel.servers

import io.norselabs.vpn.common.state.Status
import io.norselabs.vpn.common.state.ViewStateHolder
import io.norselabs.vpn.sdk.services.destination.api.City
import io.norselabs.vpn.sdk.services.destination.api.Country
import javax.inject.Inject
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

class ServersScreenStateHolder
@Inject constructor() : ViewStateHolder<ServersScreenState, ServersScreenEffect>(
  ServersScreenState(),
)

data class ServersScreenState(
  val status: Status = Status.Loading,
  val countryId: String? = null,
  val cityId: String? = null,
  val country: Country? = null,
  val city: City? = null,
  val servers: PersistentList<ServerUi> = persistentListOf(),
)

data class ServerUi(
  val id: String,
  val cityId: String,
  val name: String,
)

sealed interface ServersScreenEffect {
  data object GoBack : ServersScreenEffect
  data object GoBackToRoot : ServersScreenEffect
}
