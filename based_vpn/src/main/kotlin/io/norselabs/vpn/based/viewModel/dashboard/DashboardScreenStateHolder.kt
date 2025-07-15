package io.norselabs.vpn.based.viewModel.dashboard

import io.norselabs.vpn.common.state.Status
import io.norselabs.vpn.common.state.ViewStateHolder
import io.norselabs.vpn.core_vpn.user.UserStatus
import io.norselabs.vpn.core_vpn.vpn.Destination
import javax.inject.Inject

class DashboardScreenStateHolder
@Inject constructor() : ViewStateHolder<DashboardScreenState, DashboardScreenEffect>(
  DashboardScreenState(),
)

data class DashboardScreenState(
  val status: Status = Status.Data,
  val vpnStatus: VpnStatus = VpnStatus.Disconnected,
  val userStatus: UserStatus = UserStatus.Determining,
  val networkData: NetworkDataUi? = null,
  val destination: Destination? = null,
  val retryAttempt: Int = 0,
  val isErrorAlertVisible: Boolean = false,
  val isRatingAlertVisible: Boolean = false,
)

data class NetworkDataUi(
  val ip: String,
  val lat: Double,
  val long: Double,
)

sealed interface DashboardScreenEffect {
  data object ShowAd : DashboardScreenEffect
  data object CheckVpnPermission : DashboardScreenEffect
  data object ShowSelectServer : DashboardScreenEffect
  data object ShowSettings : DashboardScreenEffect
  data object ShowGooglePlay : DashboardScreenEffect
  data object ShowRating : DashboardScreenEffect
  data object EmailToSupport : DashboardScreenEffect
}

sealed interface VpnStatus {
  data object Disconnected : VpnStatus
  data object Connecting : VpnStatus
  data object Connected : VpnStatus
}

enum class RatingClick {
  Positive,
  Negative,
  Dismiss,
}
