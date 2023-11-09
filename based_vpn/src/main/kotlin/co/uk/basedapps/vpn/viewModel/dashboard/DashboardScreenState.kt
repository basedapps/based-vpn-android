package co.uk.basedapps.vpn.viewModel.dashboard

import co.uk.basedapps.vpn.common.state.Status
import co.uk.basedapps.vpn.common.state.ViewStateHolder
import co.uk.basedapps.vpn.storage.SelectedCity
import javax.inject.Inject

class DashboardScreenStateHolder
@Inject
constructor() : ViewStateHolder<DashboardScreenState, DashboardScreenEffect>(
  DashboardScreenState(),
)

data class DashboardScreenState(
  val status: Status = Status.Data,
  val vpnStatus: VpnStatus = VpnStatus.Disconnected,
  val isBanned: Boolean = false,
  val selectedCity: SelectedCity? = null,
  val ipAddress: String = "",
  val isErrorAlertVisible: Boolean = false,
)

sealed interface DashboardScreenEffect {
  data object CheckVpnPermission : DashboardScreenEffect
  data object ShowSelectServer : DashboardScreenEffect
  data object ShowSettings : DashboardScreenEffect
  data class ChangeMapPosition(
    val latitude: Double,
    val longitude: Double,
  ) : DashboardScreenEffect
}

sealed interface VpnStatus {
  data object Disconnected : VpnStatus
  data object Connecting : VpnStatus
  data object Connected : VpnStatus
  data object Disconnecting : VpnStatus
}
