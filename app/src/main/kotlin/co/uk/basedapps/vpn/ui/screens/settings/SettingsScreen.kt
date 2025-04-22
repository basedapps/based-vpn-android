package co.uk.basedapps.vpn.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.hilt.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import co.sentinel.based_vpn.R as BasedR
import co.uk.basedapps.vpn.R
import co.uk.basedapps.vpn.ui.screens.settings.widgets.DnsDialog
import co.uk.basedapps.vpn.ui.screens.settings.widgets.ProtocolDialog
import co.uk.basedapps.vpn.ui.screens.split_tunneling.SplitTunnelingScreen
import co.uk.basedapps.vpn.ui.theme.BasedAppColor
import co.uk.basedapps.vpn.ui.widget.TopBar
import io.norselabs.vpn.based.viewModel.settings.SettingsScreenEffect as Effect
import io.norselabs.vpn.based.viewModel.settings.SettingsScreenState as State
import io.norselabs.vpn.based.viewModel.settings.SettingsScreenViewModel
import io.norselabs.vpn.common_compose.EffectHandler
import io.norselabs.vpn.core_vpn.vpn.Protocol
import io.norselabs.vpn.core_vpn.vpn.dns.DnsProvider

class SettingsScreen : Screen {

  @Composable
  override fun Content() {
    val viewModel = getScreenModel<SettingsScreenViewModel>()
    val state by viewModel.stateHolder.state.collectAsState()

    val navigator = LocalNavigator.currentOrThrow

    EffectHandler(viewModel.stateHolder.effects) { effect ->
      when (effect) {
        is Effect.GoBack -> navigator.pop()

        is Effect.OpenTelegram -> Unit

        is Effect.SplitTunneling -> navigator.push(SplitTunnelingScreen())
      }
    }

    SettingsScreenStateless(
      state = state,
      navigateBack = viewModel::onBackClick,
      onDnsRowClick = viewModel::onDnsRowClick,
      onDnsDialogConfirmClick = viewModel::onDnsSelected,
      onDnsDialogDismissClick = viewModel::onDnsDialogDismissClick,
      onProtocolRowClick = viewModel::onProtocolRowClick,
      onProtocolDialogConfirmClick = viewModel::onProtocolSelected,
      onProtocolDialogDismissClick = viewModel::onProtocolDialogDismissClick,
      onSplitTunnelingClick = viewModel::onSplitTunnelClick,
      onLogsRowClick = viewModel::onLogsRowClick,
    )
  }
}

@Composable
fun SettingsScreenStateless(
  state: State,
  navigateBack: () -> Unit,
  onDnsRowClick: () -> Unit,
  onDnsDialogConfirmClick: (DnsProvider) -> Unit,
  onDnsDialogDismissClick: () -> Unit,
  onProtocolRowClick: () -> Unit,
  onProtocolDialogConfirmClick: (Protocol?) -> Unit,
  onProtocolDialogDismissClick: () -> Unit,
  onSplitTunnelingClick: () -> Unit,
  onLogsRowClick: () -> Unit,
) {
  Scaffold(
    containerColor = BasedAppColor.Background,
    topBar = {
      TopBar(
        title = stringResource(R.string.settings_title),
        navigateBack = navigateBack,
      )
    },
    content = { paddingValues ->
      Content(
        paddingValues = paddingValues,
        state = state,
        onDnsRowClick = onDnsRowClick,
        onDnsDialogConfirmClick = onDnsDialogConfirmClick,
        onDnsDialogDismissClick = onDnsDialogDismissClick,
        onProtocolRowClick = onProtocolRowClick,
        onProtocolDialogConfirmClick = onProtocolDialogConfirmClick,
        onProtocolDialogDismissClick = onProtocolDialogDismissClick,
        onSplitTunnelingClick = onSplitTunnelingClick,
        onLogsRowClick = onLogsRowClick,
      )
    },
  )
}

@Composable
fun Content(
  paddingValues: PaddingValues,
  state: State,
  onDnsRowClick: () -> Unit,
  onDnsDialogConfirmClick: (DnsProvider) -> Unit,
  onDnsDialogDismissClick: () -> Unit,
  onProtocolRowClick: () -> Unit,
  onProtocolDialogConfirmClick: (Protocol?) -> Unit,
  onProtocolDialogDismissClick: () -> Unit,
  onSplitTunnelingClick: () -> Unit,
  onLogsRowClick: () -> Unit,
) {
  Box {
    Column(
      modifier = Modifier
        .padding(paddingValues),
    ) {
      SettingsRow(
        title = stringResource(R.string.settings_row_dns),
        value = state.currentDnsProvider
          ?.let { stringResource(it.getLabelRes()) } ?: "",
        modifier = Modifier
          .clickable(onClick = onDnsRowClick),
      )
      HorizontalDivider(color = BasedAppColor.Divider)
      SettingsRow(
        title = stringResource(R.string.settings_row_protocol),
        value = stringResource(state.currentProtocol.getLabelRes()),
        modifier = Modifier
          .clickable(onClick = onProtocolRowClick),
      )
      HorizontalDivider(color = BasedAppColor.Divider)
      SettingsRow(
        title = stringResource(R.string.settings_row_split_tunneling),
        value = "",
        modifier = Modifier
          .clickable(onClick = onSplitTunnelingClick),
      )
      HorizontalDivider(color = BasedAppColor.Divider)
      SettingsRow(
        title = stringResource(R.string.settings_row_logs),
        value = "",
        modifier = Modifier
          .clickable(onClick = onLogsRowClick),
      )
      HorizontalDivider(color = BasedAppColor.Divider)
    }
    if (state.isDnsSelectorVisible) {
      DnsDialog(
        state = state,
        onConfirmClick = onDnsDialogConfirmClick,
        onDismissClick = onDnsDialogDismissClick,
        onDismissRequest = onDnsDialogDismissClick,
      )
    }

    if (state.isProtocolSelectorVisible) {
      ProtocolDialog(
        state = state,
        onConfirmClick = onProtocolDialogConfirmClick,
        onDismissRequest = onProtocolDialogDismissClick,
      )
    }
  }
}

@Composable
private fun SettingsRow(
  title: String,
  value: String,
  modifier: Modifier = Modifier,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .heightIn(min = 54.dp)
      .padding(16.dp)
      .fillMaxWidth(),
  ) {
    Text(
      text = title,
      overflow = TextOverflow.Ellipsis,
      maxLines = 1,
      fontSize = 16.sp,
      color = BasedAppColor.TextPrimary,
      modifier = Modifier.weight(1f),
    )
    Text(
      text = value,
      textAlign = TextAlign.End,
      overflow = TextOverflow.Ellipsis,
      maxLines = 1,
      fontSize = 16.sp,
      color = BasedAppColor.TextSecondary,
      modifier = Modifier.weight(1f),
    )
  }
}

fun DnsProvider.getLabelRes() = when (this) {
  DnsProvider.Cloudflare -> R.string.settings_dns_cloudflare
  DnsProvider.Google -> R.string.settings_dns_google
  DnsProvider.Quad9 -> R.string.settings_dns_quad9
}

fun Protocol?.getLabelRes() = when (this) {
  Protocol.WIREGUARD -> BasedR.string.settings_protocol_wireguard
  Protocol.V2RAY -> BasedR.string.settings_protocol_v2ray
  else -> BasedR.string.settings_protocol_any
}
