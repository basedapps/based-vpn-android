package io.norselabs.vpn.based.viewModel.dashboard

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.norselabs.vpn.based.storage.AppStorage
import io.norselabs.vpn.based.storage.RatingStatus
import io.norselabs.vpn.based.viewModel.dashboard.DashboardScreenEffect as Effect
import io.norselabs.vpn.common.permissions.NotificationPermissionChecker
import io.norselabs.vpn.common.permissions.NotificationPermissionStatus
import io.norselabs.vpn.common.permissions.NotificationPromptStorage
import io.norselabs.vpn.common.state.Status
import io.norselabs.vpn.common_logger.share.LogsSender
import io.norselabs.vpn.common_purchases.PurchasesManager
import io.norselabs.vpn.core_vpn.connectivity.NetworkInfoUpdater
import io.norselabs.vpn.core_vpn.user.UserInitializer
import io.norselabs.vpn.core_vpn.user.UserStatus
import io.norselabs.vpn.core_vpn.vpn.Destination
import io.norselabs.vpn.core_vpn.vpn.connector.DisconnectReason
import io.norselabs.vpn.core_vpn.vpn.connector.VPNConnector
import io.norselabs.vpn.core_vpn.vpn.destination.DestinationStorage
import io.norselabs.vpn.sdk.dvpn_client.DVPNClient
import io.norselabs.vpn.v2ray.model.VpnConnection
import io.norselabs.vpn.v2ray.repo.V2RayRepository
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import timber.log.Timber

class DashboardScreenViewModel
@Inject constructor(
  val stateHolder: DashboardScreenStateHolder,
  private val dvpnClient: DVPNClient,
  private val appStorage: AppStorage,
  private val connector: VPNConnector,
  private val vpnRepo: V2RayRepository,
  private val userInitializer: UserInitializer,
  private val destinationStorage: DestinationStorage,
  private val networkInfoUpdater: NetworkInfoUpdater,
  private val logsSender: LogsSender,
  private val purchasesManager: PurchasesManager,
  private val notificationPromptStorage: NotificationPromptStorage,
  val notificationPermissionChecker: NotificationPermissionChecker,
) : ScreenModel {

  private var connectJob: Job? = null
  private var successCardDismissJob: Job? = null
  private var connectionErrorJob: Job? = null

  private val state: DashboardScreenState
    get() = stateHolder.state.value

  init {
    observeNetworkData()
    observeConnectionState()
    observeUserStatus()
    observeInitStatus()
    observeDestination()
  }

  private fun observeNetworkData() {
    screenModelScope.launch {
      networkInfoUpdater.networkData.filterNotNull().collect { data ->
        stateHolder.updateState {
          copy(
            networkData = NetworkDataUi(
              ip = data.ip,
              lat = data.info.lat,
              long = data.info.long,
            ),
          )
        }
      }
    }
  }

  private fun observeConnectionState() {
    screenModelScope.launch {
      vpnRepo.connectionState
        .collect { state ->
          setVpnStatus(
            when (state) {
              is VpnConnection.Connected -> VpnStatus.Connected
              VpnConnection.Connecting,
              VpnConnection.CoreStarted,
              -> VpnStatus.Connecting
              VpnConnection.Disconnected -> VpnStatus.Disconnected
            },
          )
        }
    }
  }

  private fun observeUserStatus() {
    screenModelScope.launch {
      userInitializer.status.collect { userStatus ->
        Timber.tag(TAG).d("User Status: $userStatus")
        val previousUserStatus = state.userStatus
        stateHolder.updateState { copy(userStatus = userStatus) }
        val screenStatus = when (userStatus) {
          UserStatus.Enrolled,
          UserStatus.Determining,
          UserStatus.HasToken,
          UserStatus.Enrolling,
          -> Status.Data

          else -> Status.Error()
        }
        stateHolder.updateState { copy(status = screenStatus) }
        handleSuccessCardOnUserStatusChange(previousUserStatus, userStatus)
      }
    }
  }

  private fun handleSuccessCardOnUserStatusChange(
    previous: UserStatus,
    current: UserStatus,
  ) {
    if (current == UserStatus.Enrolled && previous != UserStatus.Enrolled) {
      successCardDismissJob?.cancel()
      stateHolder.updateState { copy(successCardDismissed = false) }
      successCardDismissJob = screenModelScope.launch {
        delay(SUCCESS_CARD_VISIBLE_MS)
        stateHolder.updateState { copy(successCardDismissed = true) }
      }
    } else if (current != UserStatus.Enrolled && previous == UserStatus.Enrolled) {
      successCardDismissJob?.cancel()
      successCardDismissJob = null
    }
  }

  private fun observeInitStatus() {
    screenModelScope.launch {
      dvpnClient.status.collect { initStatus ->
        Timber.tag(TAG).d("Init Status: $initStatus")
        stateHolder.updateState { copy(clientInitStatus = initStatus) }
      }
    }
  }

  private fun observeDestination() {
    screenModelScope.launch {
      destinationStorage.observe().collect { destination ->
        val previousDestination = state.destination
        stateHolder.updateState { copy(destination = destination) }
        if (previousDestination != null && previousDestination != destination) {
          connectJob?.cancelAndJoin()
          connector.disconnect(DisconnectReason.UserRequested)
          initConnection()
        }
      }
    }
  }

  private fun setVpnStatus(vpnStatus: VpnStatus) {
    Timber.tag(TAG).d("Set VPN Status: $vpnStatus")
    stateHolder.updateState {
      copy(vpnStatus = vpnStatus)
    }
  }

  private fun showConnectionError() {
    connectionErrorJob?.cancel()
    stateHolder.updateState { copy(connectionErrorVisible = true) }
    connectionErrorJob = screenModelScope.launch {
      delay(CONNECTION_ERROR_VISIBLE_MS)
      stateHolder.updateState { copy(connectionErrorVisible = false) }
    }
  }

  private fun hideConnectionError() {
    connectionErrorJob?.cancel()
    connectionErrorJob = null
    if (state.connectionErrorVisible) {
      stateHolder.updateState { copy(connectionErrorVisible = false) }
    }
  }

  fun onConnectClick() {
    if (state.vpnStatus is VpnStatus.Disconnected) {
      Timber.tag(TAG).d("Connect clicked")
      initConnection()
    }
  }

  fun onQuickConnectClick() {
    if (state.vpnStatus is VpnStatus.Disconnected) {
      Timber.tag(TAG).d("Quick Connect clicked")
      screenModelScope.launch {
        selectRandomDestination()
        initConnection()
      }
    }
  }

  fun onDisconnectClick() {
    if (state.vpnStatus !is VpnStatus.Disconnected) {
      Timber.tag(TAG).d("Disconnect clicked")
      stopVpn()
    }
  }

  private fun initConnection() {
    if (!state.isReadyToConnect) {
      Timber.tag(TAG).d("Connect ignored: not ready (status card visible)")
      return
    }
    if (state.destination == null) {
      onSelectServerClick()
      return
    }
    if (shouldSuggestNotifications()) {
      // Optimistic Connecting for the whole prompt round-trip (same pattern
      // as the VPN-permission wait), so connect controls reflect an ongoing
      // attempt while the popup is up. Every popup exit either proceeds or
      // rolls the status back to Disconnected.
      setVpnStatus(VpnStatus.Connecting)
      stateHolder.sendEffect(Effect.ShowNotificationsPopup)
      return
    }
    proceedConnection()
  }

  // The "prompt shown" flag is written only on an explicit button tap, so an
  // accidental swipe/scrim dismiss neither records the prompt nor continues
  // the flow — the next Connect tap shows the popup again.
  private fun shouldSuggestNotifications(): Boolean {
    return !notificationPromptStorage.wasPromptShown &&
      notificationPermissionChecker.refresh() != NotificationPermissionStatus.Granted
  }

  private fun proceedConnection() {
    hideConnectionError()
    setVpnStatus(VpnStatus.Connecting)
    stateHolder.sendEffect(Effect.CheckVpnPermission)
  }

  fun onNotificationsPopupConfirm() {
    notificationPromptStorage.wasPromptShown = true
    when (notificationPermissionChecker.refresh()) {
      // System dialog fires; the connect resumes on its result regardless
      // of the answer (onNotificationPermissionResult).
      NotificationPermissionStatus.Denied ->
        stateHolder.sendEffect(Effect.RequestNotificationPermission)

      // API 26-32 toggle: the notification settings screen opens and the
      // connect flow deliberately stops there — roll the status back.
      NotificationPermissionStatus.Disabled -> {
        stateHolder.sendEffect(Effect.RequestNotificationPermission)
        setVpnStatus(VpnStatus.Disconnected)
      }

      // Granted while the popup was up (e.g. from a notification shade
      // shortcut) — nothing to request, just connect.
      NotificationPermissionStatus.Granted -> proceedConnection()
    }
  }

  fun onNotificationsPopupDismiss() {
    notificationPromptStorage.wasPromptShown = true
    proceedConnection()
  }

  // Swipe/scrim dismiss: the prompt isn't recorded as shown and the connect
  // attempt is abandoned — undo the optimistic Connecting.
  fun onNotificationsPopupClosed() {
    setVpnStatus(VpnStatus.Disconnected)
  }

  fun onNotificationPermissionResult() {
    proceedConnection()
  }

  fun onPermissionsResult(isSuccess: Boolean) {
    screenModelScope.launch {
      val isSubscribed = purchasesManager.getCustomerData()
        .getOrNull()?.isSubscribed == true
      when {
        isSuccess && isSubscribed -> onAdShown()
        isSuccess -> stateHolder.sendEffect(Effect.ShowAd)
        else -> {
          // todo: show an error
          stateHolder.updateState { copy(vpnStatus = VpnStatus.Disconnected) }
        }
      }
    }
  }

  fun onAdShown() {
    connectVpn()
  }

  private fun connectVpn() {
    val destination = state.destination ?: return
    connectJob = screenModelScope.launch {
      connector.connect(destination)
        .onLeft {
          setVpnStatus(VpnStatus.Disconnected)
          showConnectionError()
        }
        .onRight { checkAppRatingRequest() }
    }
  }

  private fun selectRandomDestination() {
    destinationStorage.storeDestination(Destination.Random)
  }

  private fun stopVpn() {
    connectJob?.cancel()
    connector.disconnect(DisconnectReason.UserRequested)
    setVpnStatus(VpnStatus.Disconnected)
    if (state.destination is Destination.Deeplink) {
      stateHolder.updateState {
        copy(destination = destinationStorage.getLast())
      }
    }
  }

  fun onTryAgainClick() {
    stateHolder.updateState {
      copy(status = Status.Loading)
    }
    userInitializer.enroll()
  }

  fun onSelectServerClick() {
    stateHolder.sendEffect(Effect.ShowSelectServer)
  }

  fun onSettingsClick() {
    stateHolder.sendEffect(Effect.ShowSettings)
  }

  fun onUpdateClick() {
    stateHolder.sendEffect(Effect.ShowGooglePlay)
  }

  fun onShareLogsClick() {
    logsSender.shareLogs()
  }

  private fun checkAppRatingRequest() {
    val rating = appStorage.getRatingStatus()
    when (rating) {
      RatingStatus.New -> {
        appStorage.setRatingStatus(RatingStatus.RequestOnNext)
      }

      RatingStatus.RequestOnNext -> {
        stateHolder.sendEffect(Effect.ShowRatingPrompt)
      }

      RatingStatus.Requested -> Unit
    }
  }

  fun onRatingClick(click: RatingClick) {
    when (click) {
      RatingClick.Positive -> {
        stateHolder.sendEffect(Effect.ShowRating)
        appStorage.setRatingStatus(RatingStatus.Requested)
      }

      RatingClick.Negative -> {
        stateHolder.sendEffect(Effect.EmailToSupport)
        appStorage.setRatingStatus(RatingStatus.Requested)
      }

      RatingClick.Dismiss -> {
        appStorage.setRatingStatus(RatingStatus.New)
      }
    }
  }

  companion object {
    const val TAG = "Dashboard"
    const val SUCCESS_CARD_VISIBLE_MS = 2_000L
    const val CONNECTION_ERROR_VISIBLE_MS = 3_000L
  }
}
