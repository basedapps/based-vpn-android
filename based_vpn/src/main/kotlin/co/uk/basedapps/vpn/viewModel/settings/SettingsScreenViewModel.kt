package co.uk.basedapps.vpn.viewModel.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.uk.basedapps.vpn.network.model.Protocol
import co.uk.basedapps.vpn.storage.BasedStorage
import co.uk.basedapps.vpn.storage.LogsStorage
import co.uk.basedapps.vpn.vpn.DdsConfigurator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsScreenViewModel
@Inject constructor(
  val stateHolder: SettingsScreenStateHolder,
  private val dnsConfigurator: DdsConfigurator,
  private val storage: BasedStorage,
  private val logsStorage: LogsStorage,
) : ViewModel() {

  private val state: SettingsScreenState
    get() = stateHolder.state.value

  init {
    viewModelScope.launch {
      val dns = dnsConfigurator.getDefaultDns()
      val protocol = storage.getVpnProtocol()
      stateHolder.updateState {
        copy(
          currentDns = dns,
          currentProtocol = protocol,
        )
      }
    }
  }

  fun onDnsRowClick() {
    stateHolder.updateState { copy(isDnsSelectorVisible = true) }
  }

  fun onDnsSelected(dns: DdsConfigurator.Dns) {
    stateHolder.updateState {
      copy(
        isDnsSelectorVisible = false,
        currentDns = dns,
      )
    }
    viewModelScope.launch {
      dnsConfigurator.setDns(dns)
    }
  }

  fun onDnsDialogDismissClick() {
    stateHolder.updateState { copy(isDnsSelectorVisible = false) }
  }

  fun onProtocolRowClick() {
    stateHolder.updateState { copy(isProtocolSelectorVisible = true) }
  }

  fun onProtocolSelected(protocol: Protocol) {
    stateHolder.updateState {
      copy(
        isProtocolSelectorVisible = false,
        currentProtocol = protocol,
      )
    }
    viewModelScope.launch {
      storage.storeVpnProtocol(protocol)
    }
  }

  fun onProtocolDialogDismissClick() {
    stateHolder.updateState { copy(isProtocolSelectorVisible = false) }
  }

  fun onTelegramClick() {
    stateHolder.sendEffect(SettingsScreenEffect.OpenTelegram)
  }

  fun onLogsRowClick() {
    val logs = logsStorage.getLogs()
    stateHolder.sendEffect(
      SettingsScreenEffect.CopyLogsToClipboard(logs),
    )
  }
}
