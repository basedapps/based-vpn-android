package co.sentinel.vpn.based.viewModel.split_tunneling

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.norselabs.vpn.v2ray.repo.V2RayRepository
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltViewModel
class SplitTunnelingScreenViewModel
@Inject constructor(
  val stateHolder: SplitTunnelingScreenStateHolder,
  private val appsProvider: NetAppsProvider,
  private val v2RayRepository: V2RayRepository,
) : ViewModel() {

  private val state: SplitTunnelingScreenState
    get() = stateHolder.state.value

  init {
    initApps()
  }

  private fun initApps() {
    viewModelScope.launch(Dispatchers.IO) {
      val checkedApps = v2RayRepository.getPerAppProxySet()
      val apps = appsProvider.getNetApps()
        .map { app ->
          if (checkedApps.contains(app.packageName)) {
            app.copy(isChecked = true)
          } else {
            app
          }
        }
        .sortedBy { it.appName }
      stateHolder.updateState {
        copy(applications = apps)
      }
    }
  }

  fun onSplitTunnelingEnabled(isEnabled: Boolean) {
    v2RayRepository.enablePerAppProxy(isEnabled)
    stateHolder.updateState {
      copy(isSplitTunnelingEnabled = isEnabled)
    }
  }

  fun onBypassModeEnabled(isEnabled: Boolean) {
    v2RayRepository.enableBypass(isEnabled)
    stateHolder.updateState {
      copy(isBypassModeEnabled = isEnabled)
    }
  }

  fun onAppChecked(app: NetworkApp, isChecked: Boolean) {
    val newList = state.applications
      .filter { it.packageName != app.packageName }
      .let { it + listOf(app.copy(isChecked = isChecked)) }
      .sortedBy { it.appName }
    stateHolder.updateState {
      copy(applications = newList)
    }
    val newSet = newList
      .filter { it.isChecked }
      .map { it.packageName }
      .toSet()
    v2RayRepository.setPerAppProxySet(newSet)
  }
}
