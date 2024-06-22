package co.sentinel.vpn.based.viewModel.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.flatMap
import co.sentinel.vpn.based.app_config.AppConfig
import co.sentinel.vpn.based.network.model.Country
import co.sentinel.vpn.based.network.model.IpModel
import co.sentinel.vpn.based.network.model.Protocol
import co.sentinel.vpn.based.network.repository.BasedRepository
import co.sentinel.vpn.based.network.repository.CitiesRequest
import co.sentinel.vpn.based.state.Status
import co.sentinel.vpn.based.storage.BasedStorage
import co.sentinel.vpn.based.storage.SelectedCity
import co.sentinel.vpn.based.viewModel.dashboard.DashboardScreenEffect as Effect
import co.sentinel.vpn.based.vpn.VPNConnector
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber

@HiltViewModel
class DashboardScreenViewModel
@Inject constructor(
  val stateHolder: DashboardScreenStateHolder,
  private val repository: BasedRepository,
  private val storage: BasedStorage,
  private val vpnConnector: VPNConnector,
  private val config: AppConfig,
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
    enrollUser()
  }

  private fun enrollUser(shouldRefreshToken: Boolean = false) {
    viewModelScope.launch {
      if (isUpdateRequired()) {
        handleEnrolmentStatus(EnrollmentStatus.VersionOutdated)
        return@launch
      }

      if (shouldRefreshToken) {
        storage.clearToken()
      }
      val hasToken = if (storage.getToken().isEmpty()) {
        !getToken().isNullOrEmpty()
      } else {
        true
      }
      val enrollmentStatus = if (hasToken) {
        refreshIp(isSingle = true)
        waitUserEnrollment()
      } else {
        EnrollmentStatus.NotEnrolled
      }
      handleEnrolmentStatus(enrollmentStatus)
    }
  }

  private fun handleEnrolmentStatus(status: EnrollmentStatus) {
    when (status) {
      EnrollmentStatus.Enrolled ->
        stateHolder.updateState { copy(status = Status.Data) }

      EnrollmentStatus.NotEnrolled ->
        stateHolder.updateState { copy(status = Status.Error(false)) }

      EnrollmentStatus.Banned ->
        stateHolder.updateState {
          copy(status = Status.Error(false), isBanned = true)
        }

      EnrollmentStatus.TokenExpired ->
        enrollUser(shouldRefreshToken = true)

      EnrollmentStatus.VersionOutdated ->
        stateHolder.updateState {
          copy(status = Status.Error(false), isOutdated = true)
        }
    }
  }

  private suspend fun isUpdateRequired(): Boolean {
    val response = repository.getVersion().getOrNull()
    return if (response != null) {
      val versions = response.data
      versions.appVersion > config.getBasedAppVersion() ||
        versions.apiVersion > config.getBasedApiVersion()
    } else {
      false
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

  private suspend fun waitUserEnrollment(): EnrollmentStatus {
    val maxAttempts = 20
    repeat(maxAttempts) { attempt ->
      repository.getSession()
        .onLeft { exception ->
          val code = (exception as? HttpException)?.response()?.code()
          if (code == 401) return EnrollmentStatus.TokenExpired
        }
        .onRight { sessionRes ->
          val session = sessionRes.data
          when {
            session.isBanned -> return EnrollmentStatus.Banned
            session.isEnrolled -> return EnrollmentStatus.Enrolled
          }
        }
      if (attempt < maxAttempts - 1) delay(5.seconds)
    }
    return EnrollmentStatus.NotEnrolled
  }

  private suspend fun refreshIp(isSingle: Boolean = false) {
    val currentIp = state.ipAddress
    Timber.tag(Tag).d("-> Current IP: $currentIp")
    var refreshAttempt = 0
    var ipModel: IpModel
    do {
      Timber.tag(Tag).d("Reset connection")
      repository.resetConnection()
      delay(300)
      Timber.tag(Tag).d("Refresh IP $refreshAttempt")
      refreshAttempt++
      ipModel = repository.getIp().getOrNull()?.data ?: return
      Timber.tag(Tag).d("New IP: ${ipModel.ip}")
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
      Timber.tag(Tag).d("Tunnel state: $isConnected")
      if (isConnected) {
        setConnectedState()
      } else {
        setDisconnectedState()
      }
    }
  }

  private fun onCityChanged(city: SelectedCity?) {
    Timber.tag(Tag).d("City changed to ${city?.name} (prev: ${state.selectedCity?.name})")
    val isConnected = state.vpnStatus == VpnStatus.Connected
    if (state.selectedCity != null && isConnected) {
      disconnect()
    }
    stateHolder.updateState {
      copy(selectedCity = city)
    }
  }

  fun onConnectClick() {
    if (state.selectedCity != null) {
      when (state.vpnStatus) {
        is VpnStatus.Connecting -> disconnect()
        is VpnStatus.Connected -> disconnect()
        is VpnStatus.Disconnected -> startConnection()
      }
    } else {
      onSelectServerClick()
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
    enrollUser()
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
    vpnConnector.connect(city)
      .onRight { setConnectedState() }
      .onLeft(::handleConnectionError)
  }

  private suspend fun setConnectedState() {
    Timber.tag(Tag).d("Connected!")
    refreshIp()
    stateHolder.updateState { copy(vpnStatus = VpnStatus.Connected) }
  }

  private fun handleConnectionError(error: VPNConnector.Error) {
    Timber.tag(Tag).d("Connection failed! reason: $error")
    when (error) {
      is VPNConnector.Error.NotEnrolled,
      is VPNConnector.Error.TokenExpired,
      -> {
        stateHolder.updateState {
          copy(
            status = Status.Loading,
            vpnStatus = VpnStatus.Disconnected,
          )
        }
        enrollUser(
          shouldRefreshToken = error is VPNConnector.Error.TokenExpired,
        )
      }

      is VPNConnector.Error.Banned -> {
        stateHolder.updateState { copy(isBanned = true) }
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
    Timber.tag(Tag).d("Disconnect")
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

  companion object {
    const val Tag = "DashboardTag"
  }

  enum class EnrollmentStatus {
    Enrolled,
    NotEnrolled,
    Banned,
    TokenExpired,
    VersionOutdated,
  }
}
