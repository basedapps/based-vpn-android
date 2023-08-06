package co.uk.basedapps.vpn.ui.screens.settings

import co.uk.basedapps.vpn.common.ViewStateHolder
import co.uk.basedapps.vpn.ui.screens.countries.CountriesScreenEffect
import co.uk.basedapps.vpn.vpn.DdsConfigurator
import javax.inject.Inject

class SettingsScreenStateHolder
@Inject
constructor() : ViewStateHolder<SettingsScreenState, CountriesScreenEffect>(
  SettingsScreenState(),
)

data class SettingsScreenState(
  val currentDns: DdsConfigurator.Dns? = null,
  val dnsOptions: List<DdsConfigurator.Dns> = listOf(
    DdsConfigurator.Dns.Cloudflare,
    DdsConfigurator.Dns.Google,
    DdsConfigurator.Dns.Handshake,
  ),
  val isDnsSelectorVisible: Boolean = false,
)
