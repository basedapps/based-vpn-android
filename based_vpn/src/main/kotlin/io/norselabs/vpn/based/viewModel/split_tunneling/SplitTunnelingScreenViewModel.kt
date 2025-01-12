package io.norselabs.vpn.based.viewModel.split_tunneling

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.norselabs.vpn.based.viewModel.split_tunneling.SplitTunnelingScreenEffect as Effect
import io.norselabs.vpn.core_vpn.vpn.split_tunneling.SplitTunnelingConfigurator
import io.norselabs.vpn.core_vpn.vpn.split_tunneling.SplitTunnelingStatus
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SplitTunnelingScreenViewModel
@Inject constructor(
  val stateHolder: SplitTunnelingScreenStateHolder,
  private val appsProvider: NetAppsProvider,
  private val splitTunneling: SplitTunnelingConfigurator,
) : ScreenModel {

  private val state: SplitTunnelingScreenState
    get() = stateHolder.state.value

  init {
    initApps()
  }

  private fun initApps() {
    screenModelScope.launch(Dispatchers.IO) {
      val checkedApps = splitTunneling.getApps()
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

  fun setSplitTunnelingStatus(status: SplitTunnelingStatus) {
    splitTunneling.setStatus(status)
    stateHolder.updateState { copy(status = status) }
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
    splitTunneling.setApps(newSet)
  }

  fun onBackClick() {
    stateHolder.sendEffect(Effect.GoBack)
  }
}
