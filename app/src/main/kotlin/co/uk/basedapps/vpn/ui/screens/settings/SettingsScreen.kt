package co.uk.basedapps.vpn.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
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
import androidx.hilt.navigation.compose.hiltViewModel
import co.sentinel.vpn.based.network.model.Protocol
import co.sentinel.vpn.based.viewModel.settings.SettingsScreenState as State
import co.sentinel.vpn.based.viewModel.settings.SettingsScreenViewModel
import co.sentinel.vpn.based.vpn.DdsConfigurator
import co.uk.basedapps.vpn.R
import co.uk.basedapps.vpn.ui.screens.settings.widgets.DnsDialog
import co.uk.basedapps.vpn.ui.screens.settings.widgets.ProtocolDialog
import co.uk.basedapps.vpn.ui.theme.BasedAppColor
import co.uk.basedapps.vpn.ui.widget.TopBar

@Composable
fun SettingsScreen(navigateBack: () -> Unit) {

  val viewModel = hiltViewModel<SettingsScreenViewModel>()
  val state by viewModel.stateHolder.state.collectAsState()

  SettingsScreenStateless(
    state = state,
    navigateBack = navigateBack,
    onDnsRowClick = viewModel::onDnsRowClick,
    onDnsDialogConfirmClick = viewModel::onDnsSelected,
    onDnsDialogDismissClick = viewModel::onDnsDialogDismissClick,
    onProtocolRowClick = viewModel::onProtocolRowClick,
    onProtocolDialogConfirmClick = viewModel::onProtocolSelected,
    onProtocolDialogDismissClick = viewModel::onProtocolDialogDismissClick,
  )
}

@Composable
fun SettingsScreenStateless(
  state: State,
  navigateBack: () -> Unit,
  onDnsRowClick: () -> Unit,
  onDnsDialogConfirmClick: (DdsConfigurator.Dns) -> Unit,
  onDnsDialogDismissClick: () -> Unit,
  onProtocolRowClick: () -> Unit,
  onProtocolDialogConfirmClick: (Protocol) -> Unit,
  onProtocolDialogDismissClick: () -> Unit,
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
      )
    },
  )
}

@Composable
fun Content(
  paddingValues: PaddingValues,
  state: State,
  onDnsRowClick: () -> Unit,
  onDnsDialogConfirmClick: (DdsConfigurator.Dns) -> Unit,
  onDnsDialogDismissClick: () -> Unit,
  onProtocolRowClick: () -> Unit,
  onProtocolDialogConfirmClick: (Protocol) -> Unit,
  onProtocolDialogDismissClick: () -> Unit,
) {
  Box {
    Column(
      modifier = Modifier
        .padding(paddingValues),
    ) {
      SettingsRow(
        title = stringResource(R.string.settings_row_dns),
        value = state.currentDns
          ?.let { stringResource(it.getLabelRes()) } ?: "",
        modifier = Modifier
          .clickable(onClick = onDnsRowClick),
      )
      Divider(color = BasedAppColor.Divider)
      SettingsRow(
        title = stringResource(R.string.settings_row_protocol),
        value = state.currentProtocol?.labelRes
          ?.let { stringResource(it) } ?: "",
        modifier = Modifier
          .clickable(onClick = onProtocolRowClick),
      )
      Divider(color = BasedAppColor.Divider)
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
        onDismissClick = onProtocolDialogDismissClick,
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
      modifier = Modifier
        .weight(1f),

    )
  }
}

fun DdsConfigurator.Dns.getLabelRes() = when (this) {
  DdsConfigurator.Dns.Cloudflare -> R.string.settings_dns_cloudflare
  DdsConfigurator.Dns.Google -> R.string.settings_dns_google
  DdsConfigurator.Dns.Handshake -> R.string.settings_dns_handshake
}
