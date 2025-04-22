package io.norselabs.vpn.based.viewModel.settings

import io.norselabs.vpn.based.app_config.AppConfig
import io.norselabs.vpn.based.viewModel.settings.dto.AppLang
import io.norselabs.vpn.common.state.ViewStateHolder
import io.norselabs.vpn.core_vpn.vpn.Protocol
import io.norselabs.vpn.core_vpn.vpn.dns.DnsProvider
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

class SettingsScreenStateHolder
@Inject constructor(
  config: AppConfig,
) : ViewStateHolder<SettingsScreenState, SettingsScreenEffect>(
  SettingsScreenState(
    appVersion = config.getAppVersion(),
  ),
)

data class SettingsScreenState(
  val currentDnsProvider: DnsProvider? = null,
  val dnsOptions: ImmutableList<DnsProvider> = persistentListOf(
    DnsProvider.Quad9,
    DnsProvider.Cloudflare,
    DnsProvider.Google,
  ),
  val isDnsSelectorVisible: Boolean = false,

  val currentProtocol: Protocol? = null,
  val protocolOptions: ImmutableList<Protocol> = persistentListOf(
    Protocol.WIREGUARD,
    Protocol.V2RAY,
  ),
  val isProtocolSelectorVisible: Boolean = false,

  val currentLang: AppLang? = null,
  val langOptions: ImmutableList<AppLang> = persistentListOf(),

  val appVersion: String = "",
)

sealed interface SettingsScreenEffect {
  data object GoBack : SettingsScreenEffect
  data object OpenTelegram : SettingsScreenEffect
  data object SplitTunneling : SettingsScreenEffect
}
