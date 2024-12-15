package co.sentinel.vpn.based.viewModel.split_tunneling

import android.graphics.drawable.Drawable
import co.sentinel.vpn.based.vpn.SplitTunnelingConfigurator
import co.sentinel.vpn.based.vpn.SplitTunnelingStatus
import io.norselabs.vpn.common.state.ViewStateHolder
import javax.inject.Inject

class SplitTunnelingScreenStateHolder
@Inject constructor(
  splitTunneling: SplitTunnelingConfigurator,
) : ViewStateHolder<SplitTunnelingScreenState, SplitTunnelingScreenEffect>(
  SplitTunnelingScreenState(
    status = splitTunneling.getStatus(),
  ),
)

data class SplitTunnelingScreenState(
  val status: SplitTunnelingStatus,
  val applications: List<NetworkApp> = emptyList(),
)

data class NetworkApp(
  val appName: String,
  val packageName: String,
  val appIcon: Drawable,
  val isSystemApp: Boolean,
  val isChecked: Boolean,
)

sealed interface SplitTunnelingScreenEffect
