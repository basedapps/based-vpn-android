package co.sentinel.vpn.based.viewModel.settings

import co.sentinel.vpn.based.app_config.AppConfig
import co.sentinel.vpn.based.network.model.Protocol
import co.sentinel.vpn.based.state.ViewStateHolder
import co.sentinel.vpn.based.viewModel.settings.dto.AppLang
import co.sentinel.vpn.based.vpn.DdsConfigurator
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

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
  val dnsOptions: ImmutableList<DdsConfigurator.Dns> = persistentListOf(
    DdsConfigurator.Dns.Cloudflare,
    DdsConfigurator.Dns.Google,
    DdsConfigurator.Dns.Handshake,
  ),
  val isDnsSelectorVisible: Boolean = false,

  val currentProtocol: Protocol? = null,
  val protocolOptions: ImmutableList<Protocol> = persistentListOf(
    Protocol.WIREGUARD,
    Protocol.V2RAY,
    Protocol.NONE,
  ),
  val isProtocolSelectorVisible: Boolean = false,

  val langOptions: ImmutableList<AppLang> = persistentListOf(),

  val appVersion: String = "",
)

sealed interface SettingsScreenEffect {
  data object OpenTelegram : SettingsScreenEffect
  data object ShareLogs : SettingsScreenEffect
}
