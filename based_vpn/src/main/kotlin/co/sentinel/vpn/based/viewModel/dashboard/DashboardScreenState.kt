package co.sentinel.vpn.based.viewModel.dashboard

import co.sentinel.vpn.based.state.Status
import co.sentinel.vpn.based.state.ViewStateHolder
import co.sentinel.vpn.based.storage.SelectedCity
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
  val isOutdated: Boolean = false,
  val selectedCity: SelectedCity? = null,
  val ipAddress: String = "",
  val isErrorAlertVisible: Boolean = false,
)

sealed interface DashboardScreenEffect {
  data object ShowAd : DashboardScreenEffect
  data object CheckVpnPermission : DashboardScreenEffect
  data object ShowSelectServer : DashboardScreenEffect
  data object ShowSettings : DashboardScreenEffect
  data object ShowGooglePlay : DashboardScreenEffect
  data class ChangeMapPosition(
    val latitude: Double,
    val longitude: Double,
  ) : DashboardScreenEffect
}

sealed interface VpnStatus {
  data object Disconnected : VpnStatus
  data class Connecting(val isQuick: Boolean) : VpnStatus
  data object Connected : VpnStatus
}
