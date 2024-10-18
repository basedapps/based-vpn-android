package co.sentinel.vpn.based.viewModel.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.flatMap
import co.sentinel.vpn.based.core.user.UserInitializer
import co.sentinel.vpn.based.core.user.UserStatus
import co.sentinel.vpn.based.core.vpn.Destination
import co.sentinel.vpn.based.core.vpn.Protocol
import co.sentinel.vpn.based.core.vpn.VPNConnector
import co.sentinel.vpn.based.network.model.Country
import co.sentinel.vpn.based.network.model.IpModel
import co.sentinel.vpn.based.network.repository.BasedRepository
import co.sentinel.vpn.based.network.repository.CitiesRequest
import co.sentinel.vpn.based.state.Status
import co.sentinel.vpn.based.storage.BasedStorage
import co.sentinel.vpn.based.storage.RatingStatus
import co.sentinel.vpn.based.storage.SelectedCity
import co.sentinel.vpn.based.viewModel.dashboard.DashboardScreenEffect as Effect
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class DashboardScreenViewModel
@Inject constructor(
  val stateHolder: DashboardScreenStateHolder,
  private val repository: BasedRepository,
  private val storage: BasedStorage,
  private val vpnConnector: VPNConnector,
  private val userInitializer: UserInitializer,
) : ViewModel() {

  private val state: DashboardScreenState
    get() = stateHolder.state.value

  private var startConnectionJob: Job? = null

  init {
    initialize()
    observeSelectedCity()
    checkConnection()
  }

  private fun initialize() {
    stateHolder.updateState { copy(status = Status.Loading) }
    viewModelScope.launch {
      userInitializer.status
        .onEach { status ->
          if (status == UserStatus.Enrolled) {
            refreshIp(isSingle = true)
          }
        }
        .collect(::handleEnrolmentStatus)
    }
    userInitializer.enroll()
  }

  private fun handleEnrolmentStatus(status: UserStatus) {
    when (status) {
      UserStatus.Init -> Unit

      UserStatus.Enrolled -> stateHolder.updateState {
        copy(status = Status.Data, enrolmentStatus = status)
      }

      UserStatus.NotEnrolled,
      UserStatus.Banned,
      UserStatus.VersionOutdated,
      UserStatus.Failed,
      -> stateHolder.updateState {
        copy(status = Status.Error(false), enrolmentStatus = status)
      }
    }
  }

  private suspend fun refreshIp(isSingle: Boolean = false) {
    val currentIp = state.ipAddress
    Timber.tag(TAG).d("-> Current IP: $currentIp")
    var refreshAttempt = 0
    var ipModel: IpModel
    do {
      Timber.tag(TAG).d("Reset connection")
      repository.resetConnection()
      delay(300)
      Timber.tag(TAG).d("Refresh IP $refreshAttempt")
      refreshAttempt++
      ipModel = repository.getIp().getOrNull()?.data ?: return
      Timber.tag(TAG).d("New IP: ${ipModel.ip}")
      if (isSingle) break
      delay(1000)
    } while (ipModel.ip == currentIp && refreshAttempt <= 3)
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
      val isConnected = vpnConnector.isConnected()
      Timber.tag(TAG).d("Tunnel state: $isConnected")
      if (isConnected) {
        setConnectedState()
      } else {
        setDisconnectedState()
      }
    }
  }

  private fun onCityChanged(city: SelectedCity?) {
    Timber.tag(TAG).d("City changed to ${city?.name} (prev: ${state.selectedCity?.name})")
    val isConnected = state.vpnStatus is VpnStatus.Connected
    if (state.selectedCity != null && isConnected) {
      disconnect()
    }
    stateHolder.updateState {
      copy(selectedCity = city)
    }
  }

  fun onConnectClick() {
    if (state.selectedCity != null) {
      if (state.vpnStatus is VpnStatus.Disconnected) {
        startConnection()
      }
    } else {
      onSelectServerClick()
    }
  }

  fun onDisconnectClick() {
    if (state.vpnStatus !is VpnStatus.Disconnected) {
      disconnect()
    }
  }

  fun onQuickConnectClick() {
    startConnection(isQuick = true)
  }

  fun onSelectServerClick() {
    stateHolder.sendEffect(Effect.ShowSelectServer)
  }

  private fun startConnection(isQuick: Boolean = false) {
    stateHolder.updateState { copy(vpnStatus = VpnStatus.Connecting(isQuick)) }
    stateHolder.sendEffect(Effect.CheckVpnPermission)
  }

  fun onPermissionsResult(isSuccess: Boolean) {
    if (!isSuccess) {
      stateHolder.updateState { copy(vpnStatus = VpnStatus.Disconnected) }
    } else {
      showAd()
    }
  }

  private fun showAd() {
    stateHolder.sendEffect(Effect.ShowAd)
  }

  fun onAdShown() {
    establishConnection()
  }

  fun onSettingsClick() {
    stateHolder.sendEffect(Effect.ShowSettings)
  }

  fun onTryAgainClick() {
    stateHolder.updateState { copy(status = Status.Error(true)) }
    userInitializer.enroll()
  }

  fun onUpdateClick() {
    stateHolder.sendEffect(Effect.ShowGooglePlay)
  }

  private fun establishConnection() {
    if ((state.vpnStatus as? VpnStatus.Connecting)?.isQuick == true) {
      establishQuickConnection()
    } else {
      establishBasicConnection()
    }
  }

  private fun establishBasicConnection() {
    val city = state.selectedCity
    if (city == null) {
      stateHolder.updateState { copy(vpnStatus = VpnStatus.Disconnected) }
      return
    }
    startConnectionJob = viewModelScope.launch {
      connectToCity(city)
    }
  }

  private fun establishQuickConnection() {
    startConnectionJob = viewModelScope.launch {
      val protocol = storage.getVpnProtocol().takeIf { it != Protocol.NONE }
      var country: Country? = null
      repository.getCountries(protocol)
        .flatMap { countries ->
          country = countries.data.random()
          repository.getCities(CitiesRequest(country!!.id, protocol))
        }
        .onRight { cities ->
          val city = cities.data.random()
          val selectedCity = storage.storeSelectedCity(country!!, city)
          connectToCity(selectedCity)
        }
        .onLeft { handleConnectionError(VPNConnector.Error.QuickConnection) }
    }
  }

  private suspend fun connectToCity(city: SelectedCity) {
    val destination = Destination.City(
      cityId = city.id,
      countryId = city.countryId,
    )
    vpnConnector.connect(destination)
      .onRight { setConnectedState() }
      .onLeft(::handleConnectionError)
  }

  private suspend fun setConnectedState() {
    Timber.tag(TAG).d("Connected!")
    refreshIp()
    val isQuick = (state.vpnStatus as? VpnStatus.Connecting)?.isQuick ?: false
    stateHolder.updateState {
      copy(vpnStatus = VpnStatus.Connected(isQuick))
    }
    checkAppRatingRequest()
  }

  private fun handleConnectionError(error: VPNConnector.Error) {
    Timber.tag(TAG).d("Connection failed! reason: $error")
    when (error) {
      is VPNConnector.Error.UserToken -> {
        stateHolder.updateState {
          copy(
            status = Status.Loading,
            vpnStatus = VpnStatus.Disconnected,
          )
        }
        userInitializer.enroll()
      }

      else -> {
        stateHolder.updateState {
          copy(
            vpnStatus = VpnStatus.Disconnected,
            isErrorAlertVisible = true,
          )
        }
      }
    }
  }

  private fun disconnect() {
    Timber.tag(TAG).d("Disconnect")
    startConnectionJob?.cancel()
    setDisconnectedState()
    viewModelScope.launch {
      vpnConnector.disconnect()
      refreshIp()
    }
  }

  private fun setDisconnectedState() {
    stateHolder.updateState { copy(vpnStatus = VpnStatus.Disconnected) }
  }

  fun onAlertConfirmClick() {
    stateHolder.updateState { copy(isErrorAlertVisible = false) }
  }

  fun onAlertDismissRequest() {
    stateHolder.updateState { copy(isErrorAlertVisible = false) }
  }

  private fun checkAppRatingRequest() {
    val rating = storage.getRatingStatus()
    when (rating) {
      RatingStatus.New -> {
        storage.setRatingStatus(RatingStatus.RequestOnNext)
      }

      RatingStatus.RequestOnNext -> {
        stateHolder.updateState { copy(isRatingAlertVisible = true) }
      }

      RatingStatus.Requested -> Unit
    }
  }

  fun onRatingClick(click: RatingClick) {
    when (click) {
      RatingClick.Positive -> {
        stateHolder.sendEffect(Effect.ShowRating)
        storage.setRatingStatus(RatingStatus.Requested)
      }

      RatingClick.Negative -> {
        stateHolder.sendEffect(Effect.EmailToSupport)
        storage.setRatingStatus(RatingStatus.Requested)
      }

      RatingClick.Dismiss -> {
        storage.setRatingStatus(RatingStatus.New)
      }
    }
    stateHolder.updateState { copy(isRatingAlertVisible = false) }
  }

  companion object {
    const val TAG = "DashboardTag"
  }
}
