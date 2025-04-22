package io.norselabs.vpn.based.viewModel.settings

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.norselabs.vpn.based.language.LanguageManager
import io.norselabs.vpn.based.viewModel.settings.SettingsScreenEffect as Effect
import io.norselabs.vpn.based.viewModel.settings.dto.AppLang
import io.norselabs.vpn.based.vpn.DdsConfigurator
import io.norselabs.vpn.common_logger.share.LogsSender
import io.norselabs.vpn.core_vpn.storage.CoreStorage
import io.norselabs.vpn.core_vpn.vpn.Protocol
import javax.inject.Inject
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch

class SettingsScreenViewModel
@Inject constructor(
  val stateHolder: SettingsScreenStateHolder,
  private val dnsConfigurator: DdsConfigurator,
  private val coreStorage: CoreStorage,
  private val logsSender: LogsSender,
) : ScreenModel {

  private val state: SettingsScreenState
    get() = stateHolder.state.value

  init {
    screenModelScope.launch {
      val dns = dnsConfigurator.getDefaultDns()
      val protocol = coreStorage.getVpnProtocol()
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
    screenModelScope.launch {
      dnsConfigurator.setDns(dns)
    }
  }

  fun onDnsDialogDismissClick() {
    stateHolder.updateState { copy(isDnsSelectorVisible = false) }
  }

  fun onProtocolRowClick() {
    stateHolder.updateState { copy(isProtocolSelectorVisible = true) }
  }

  fun onProtocolSelected(protocol: Protocol?) {
    stateHolder.updateState {
      copy(
        isProtocolSelectorVisible = false,
        currentProtocol = protocol,
      )
    }
    screenModelScope.launch {
      coreStorage.setVpnProtocol(protocol)
    }
  }

  fun onProtocolDialogDismissClick() {
    stateHolder.updateState { copy(isProtocolSelectorVisible = false) }
  }

  fun onSplitTunnelClick() {
    stateHolder.sendEffect(Effect.SplitTunneling)
  }

  fun onTelegramClick() {
    stateHolder.sendEffect(Effect.OpenTelegram)
  }

  fun onLogsRowClick() {
    logsSender.shareLogs()
  }

  fun setSupportedLanguages(languages: List<AppLang>) {
    val appLang = LanguageManager.getLanguage()
    stateHolder.updateState {
      copy(
        currentLang = languages.firstOrNull { it.code == appLang },
        langOptions = languages.toPersistentList(),
      )
    }
  }

  fun onLanguageSelected(lang: AppLang) {
    LanguageManager.setLanguage(lang.code)
    stateHolder.updateState {
      copy(currentLang = lang)
    }
  }

  fun onBackClick() {
    stateHolder.sendEffect(Effect.GoBack)
  }
}
