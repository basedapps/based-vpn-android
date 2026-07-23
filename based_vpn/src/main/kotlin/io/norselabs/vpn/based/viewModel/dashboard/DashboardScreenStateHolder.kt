package io.norselabs.vpn.based.viewModel.dashboard

import io.norselabs.vpn.common.state.Status
import io.norselabs.vpn.common.state.ViewStateHolder
import io.norselabs.vpn.core_vpn.user.UserStatus
import io.norselabs.vpn.core_vpn.vpn.Destination
import io.norselabs.vpn.sdk.dvpn_client.InitStatus
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
  val clientInitStatus: InitStatus = InitStatus.Connecting(attempt = 0, total = 0),
  val successCardDismissed: Boolean = false,
  val connectionErrorVisible: Boolean = false,
  val isRatingAlertVisible: Boolean = false,
) {
  val cardState: ConnectionCardState? = when {
    connectionErrorVisible -> {
      ConnectionCardState.ConnectionError
    }

    status is Status.Error ||
      clientInitStatus is InitStatus.Failed ||
      userStatus == UserStatus.Failed -> {
      ConnectionCardState.Error
    }

    clientInitStatus is InitStatus.Connecting -> {
      ConnectionCardState.Loading.MirrorDiscovery(
        attempt = clientInitStatus.attempt,
        total = clientInitStatus.total,
      )
    }

    userStatus.order <= UserStatus.Enrolling.order -> {
      ConnectionCardState.Loading.UserEnrollment
    }

    userStatus == UserStatus.Enrolled && !successCardDismissed -> {
      ConnectionCardState.Success
    }

    else -> null
  }

  // Connecting is blocked while a "busy" status card is showing — mirror
  // discovery / user enrollment still in progress, or an error being surfaced.
  // The Success card is the exception: it only appears once the user is
  // Enrolled and the client is Connected (no errors), so the readiness
  // conditions are already met and connecting must stay available while it is
  // briefly on screen.
  val isReadyToConnect: Boolean =
    cardState == null || cardState is ConnectionCardState.Success
}

sealed interface ConnectionCardState {
  sealed interface Loading : ConnectionCardState {
    data class MirrorDiscovery(val attempt: Int, val total: Int) : Loading
    data object UserEnrollment : Loading
  }

  data object Success : ConnectionCardState
  data object Error : ConnectionCardState
  data object ConnectionError : ConnectionCardState
}

data class NetworkDataUi(
  val ip: String,
  val lat: Double,
  val long: Double,
)

sealed interface DashboardScreenEffect {
  data object ShowAd : DashboardScreenEffect
  data object CheckVpnPermission : DashboardScreenEffect
  data object ShowNotificationsPopup : DashboardScreenEffect
  data object RequestNotificationPermission : DashboardScreenEffect
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
