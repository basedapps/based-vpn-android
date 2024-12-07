package co.sentinel.vpn.based.viewModel.split_tunneling

import android.graphics.drawable.Drawable
import io.norselabs.vpn.common.state.ViewStateHolder
import io.norselabs.vpn.v2ray.repo.V2RayRepository
import javax.inject.Inject

class SplitTunnelingScreenStateHolder
@Inject constructor(
  v2RayRepository: V2RayRepository,
) : ViewStateHolder<SplitTunnelingScreenState, SplitTunnelingScreenEffect>(
  SplitTunnelingScreenState(
    isSplitTunnelingEnabled = v2RayRepository.isPerAppProxyEnabled(),
    isBypassModeEnabled = v2RayRepository.isBypassEnabled(),
  ),
)

data class SplitTunnelingScreenState(
  val isSplitTunnelingEnabled: Boolean,
  val isBypassModeEnabled: Boolean,
  val applications: List<NetworkApp> = emptyList(),
)

data class NetworkApp(
  val appName: String,
  val packageName: String,
  val appIcon: Drawable,
  val isSystemApp: Boolean,
  val isChecked: Boolean,
)

sealed interface SplitTunnelingScreenEffect {
  data object Temp : SplitTunnelingScreenEffect
}
