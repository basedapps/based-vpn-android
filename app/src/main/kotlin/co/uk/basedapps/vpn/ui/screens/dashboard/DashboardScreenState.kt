package co.uk.basedapps.vpn.ui.screens.dashboard

import co.uk.basedapps.vpn.common.Status
import co.uk.basedapps.vpn.common.ViewStateHolder
import co.uk.basedapps.vpn.storage.SelectedCity
import javax.inject.Inject

class DashboardScreenStateHolder
@Inject
constructor() : ViewStateHolder<DashboardScreenState, DashboardScreenEffect>(
  DashboardScreenState(),
)

data class DashboardScreenState(
  val status: Status = Status.Data,
  val selectedCity: SelectedCity? = null,
  val ipAddress: String = "",
  val isConnected: Boolean = false,
  val isErrorAlertVisible: Boolean = false,
)

sealed interface DashboardScreenEffect {
  object CheckVpnPermission : DashboardScreenEffect
  object ShowSelectServer : DashboardScreenEffect
  object ShowSettings : DashboardScreenEffect
  data class ChangeMapPosition(
    val latitude: Double,
    val longitude: Double,
  ) : DashboardScreenEffect
}
