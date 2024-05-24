package co.sentinel.vpn.based.viewModel.settings

import co.sentinel.vpn.based.app_config.AppConfig
import co.sentinel.vpn.based.network.model.Protocol
import co.sentinel.vpn.based.state.ViewStateHolder
import co.sentinel.vpn.based.vpn.DdsConfigurator
import javax.inject.Inject

class SettingsScreenStateHolder
@Inject
constructor(
  config: AppConfig,
) : ViewStateHolder<SettingsScreenState, SettingsScreenEffect>(
  SettingsScreenState(
    appVersion = config.getAppVersion(),
  ),
)

data class SettingsScreenState(
  val currentDns: DdsConfigurator.Dns? = null,
  val dnsOptions: List<DdsConfigurator.Dns> = listOf(
    DdsConfigurator.Dns.Cloudflare,
    DdsConfigurator.Dns.Google,
    DdsConfigurator.Dns.Handshake,
  ),
  val isDnsSelectorVisible: Boolean = false,

  val currentProtocol: Protocol? = null,
  val protocolOptions: List<Protocol> = listOf(
    Protocol.WIREGUARD,
    Protocol.V2RAY,
    Protocol.NONE,
  ),
  val isProtocolSelectorVisible: Boolean = false,

  val appVersion: String = "",
)

sealed interface SettingsScreenEffect {
  data object OpenTelegram : SettingsScreenEffect
  data class CopyLogsToClipboard(val logs: String) : SettingsScreenEffect
}
