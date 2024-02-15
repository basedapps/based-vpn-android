package co.uk.basedapps.vpn.viewModel.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.uk.basedapps.domain.extension.isNotNullOrEmpty
import co.uk.basedapps.domain.functional.getOrNull
import co.uk.basedapps.domain.functional.requireLeft
import co.uk.basedapps.domain.functional.requireRight
import co.uk.basedapps.vpn.common.provider.AppDetailsProvider
import co.uk.basedapps.vpn.common.state.Status
import co.uk.basedapps.vpn.network.model.IpModel
import co.uk.basedapps.vpn.network.repository.BasedRepository
import co.uk.basedapps.vpn.storage.BasedStorage
import co.uk.basedapps.vpn.storage.SelectedCity
import co.uk.basedapps.vpn.viewModel.dashboard.DashboardScreenEffect as Effect
import co.uk.basedapps.vpn.vpn.VPNConnector
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
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
  private val provider: AppDetailsProvider,
) : ViewModel() {

  private val state: DashboardScreenState
    get() = stateHolder.state.value

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
        getToken().isNotNullOrEmpty()
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
    val response = repository.getVersion()
    return if (response.isRight) {
      val versions = response.requireRight().data
      versions.appVersion > provider.getBasedAppVersion() ||
        versions.apiVersion > provider.getBasedApiVersion()
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
      val sessionRes = repository.getSession()
      if (sessionRes.isLeft) {
        val code = (sessionRes.requireLeft() as? HttpException)?.response()?.code()
        if (code == 401) return EnrollmentStatus.TokenExpired
      }
      val session = sessionRes.getOrNull()?.data
      when {
        session?.isBanned == true -> return EnrollmentStatus.Banned
        session?.isEnrolled == true -> return EnrollmentStatus.Enrolled
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
        VpnStatus.Connecting -> disconnect()
        VpnStatus.Connected -> disconnect()
        VpnStatus.Disconnecting -> Unit
        VpnStatus.Disconnected -> checkVpnPermission()
      }
    } else {
      onSelectServerClick()
    }
  }

  fun onSelectServerClick() {
    stateHolder.sendEffect(Effect.ShowSelectServer)
  }

  private fun checkVpnPermission() {
    stateHolder.updateState { copy(vpnStatus = VpnStatus.Connecting) }
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
    val city = state.selectedCity
    if (city == null) {
      stateHolder.updateState { copy(vpnStatus = VpnStatus.Disconnected) }
      return
    }
    viewModelScope.launch {
      vpnConnector.connect(city)
        .foldSuspend(
          fnR = { setConnectedState() },
          fnL = ::handleConnectionError,
        )
    }
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
    stateHolder.updateState { copy(vpnStatus = VpnStatus.Disconnecting) }
    Timber.tag(Tag).d("Disconnect")
    viewModelScope.launch {
      vpnConnector.disconnect()
      refreshIp()
      stateHolder.updateState { copy(vpnStatus = VpnStatus.Disconnected) }
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

  enum class EnrollmentStatus {
    Enrolled,
    NotEnrolled,
    Banned,
    TokenExpired,
    VersionOutdated,
  }
}
