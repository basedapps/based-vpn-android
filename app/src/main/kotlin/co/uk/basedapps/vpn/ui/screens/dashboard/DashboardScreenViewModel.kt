package co.uk.basedapps.vpn.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.uk.basedapps.domain.extension.isNotNullOrEmpty
import co.uk.basedapps.domain.functional.getOrNull
import co.uk.basedapps.vpn.common.Status
import co.uk.basedapps.vpn.network.BasedRepository
import co.uk.basedapps.vpn.network.IpModel
import co.uk.basedapps.vpn.storage.BasedStorage
import co.uk.basedapps.vpn.storage.SelectedCity
import co.uk.basedapps.vpn.ui.screens.dashboard.DashboardScreenEffect as Effect
import co.uk.basedapps.vpn.vpn.VPNConnector
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class DashboardScreenViewModel
@Inject constructor(
  val stateHolder: DashboardScreenStateHolder,
  private val repository: BasedRepository,
  private val storage: BasedStorage,
  private val vpnConnector: VPNConnector,
) : ViewModel() {

  private val state: DashboardScreenState
    get() = stateHolder.state.value

  init {
    initialize()
    observeSelectedCity()
    checkConnection()
  }

  private fun initialize() {
    viewModelScope.launch {
      stateHolder.updateState { copy(status = Status.Loading) }
      val hasToken = if (storage.getToken().isEmpty()) {
        getToken().isNotNullOrEmpty()
      } else {
        true
      }
      if (hasToken) {
        refreshIp()
        stateHolder.updateState { copy(status = Status.Data) }
      } else {
        stateHolder.updateState { copy(status = Status.Error(false)) }
      }
    }
  }

  private suspend fun getToken(): String? {
    val token = repository.registerDevice()
      .getOrNull()?.data?.token
    if (token != null) {
      Timber.tag(Tag).d("Token has been updated")
      storage.storeToken(token)
    }
    return token
  }

  private suspend fun refreshIp() {
    val currentIp = state.ipAddress
    Timber.tag(Tag).d("-> Current IP: $currentIp")
    var refreshAttempt = 0
    var ipModel: IpModel
    do {
      Timber.tag(Tag).d("Refresh IP $refreshAttempt")
      refreshAttempt++
      ipModel = repository.getIp().getOrNull()?.data ?: return
      Timber.tag(Tag).d("New IP: ${ipModel.ip}")
      delay(1000)
    } while (ipModel.ip == currentIp && refreshAttempt <= 5)
    stateHolder.updateState { copy(ipAddress = ipModel.ip) }
    stateHolder.sendEffect(
      Effect.ChangeMapPosition(
        latitude = ipModel.latitude,
        longitude = ipModel.longitude,
      ),
    )
  }

  private fun observeSelectedCity() {
    viewModelScope.launch {
      storage.observeSelectedCity()
        .collect(::onCityChanged)
    }
  }

  private fun checkConnection() {
    viewModelScope.launch {
      val tunnel = vpnConnector.getConnection() ?: return@launch
      Timber.tag(Tag).d("Active tunnel was found: ${tunnel::class}")
      if (tunnel.serverId == state.selectedCity?.serverId) {
        setConnectedState()
      } else {
        disconnect()
      }
    }
  }

  private fun onCityChanged(city: SelectedCity?) {
    Timber.tag(Tag).d("City changed to ${city?.name} (prev: ${state.selectedCity?.name})")
    if (state.selectedCity != null) {
      disconnect()
    }
    stateHolder.updateState {
      copy(selectedCity = city)
    }
  }

  fun onConnectClick() {
    if (state.selectedCity != null) {
      val isConnected = state.isConnected
      if (isConnected) {
        disconnect()
      } else {
        checkVpnPermission()
      }
    } else {
      onSelectServerClick()
    }
  }

  fun onSelectServerClick() {
    stateHolder.sendEffect(Effect.ShowSelectServer)
  }

  private fun checkVpnPermission() {
    stateHolder.sendEffect(Effect.CheckVpnPermission)
  }

  fun onSettingsClick() {
    stateHolder.sendEffect(Effect.ShowSettings)
  }

  fun onTryAgainClick() {
    stateHolder.updateState { copy(status = Status.Error(true)) }
    viewModelScope.launch {
      val hasToken = getToken().isNotNullOrEmpty()
      if (hasToken) {
        refreshIp()
        stateHolder.updateState { copy(status = Status.Data) }
      } else {
        stateHolder.updateState { copy(status = Status.Error(false)) }
      }
    }
  }

  fun onPermissionsResult(isSuccess: Boolean) {
    if (!isSuccess) return
    val city = state.selectedCity ?: return
    stateHolder.updateState { copy(status = Status.Loading) }
    viewModelScope.launch {
      vpnConnector.connect(city)
        .foldSuspend(
          fnR = { setConnectedState() },
          fnL = { showConnectionFail() },
        )
    }
  }

  private suspend fun setConnectedState() {
    Timber.tag(Tag).d("Connected!")
    refreshIp()
    stateHolder.updateState {
      copy(status = Status.Data, isConnected = true)
    }
  }

  private fun showConnectionFail() {
    Timber.tag(Tag).d("Connection failed!")
    stateHolder.updateState {
      copy(
        status = Status.Data,
        isConnected = false,
        isErrorAlertVisible = true,
      )
    }
  }

  private fun disconnect() {
    stateHolder.updateState { copy(status = Status.Loading) }
    Timber.tag(Tag).d("Disconnect")
    viewModelScope.launch {
      vpnConnector.disconnect()
      refreshIp()
      stateHolder.updateState { copy(isConnected = false, status = Status.Data) }
    }
  }

  fun onAlertConfirmClick() {
    stateHolder.updateState { copy(isErrorAlertVisible = false) }
  }

  fun onAlertDismissRequest() {
    stateHolder.updateState { copy(isErrorAlertVisible = false) }
  }

  companion object {
    const val Tag = "DashboardTag"
  }
}
