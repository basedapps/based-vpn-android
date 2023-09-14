package co.uk.basedapps.vpn.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import co.uk.basedapps.vpn.R
import co.uk.basedapps.vpn.ui.screens.settings.SettingsScreenState as State
import co.uk.basedapps.vpn.ui.theme.BasedAppColor
import co.uk.basedapps.vpn.ui.widget.TopBar
import co.uk.basedapps.vpn.vpn.DdsConfigurator

@Composable
fun SettingsScreen(
  navigateBack: () -> Unit,
) {

  val viewModel = hiltViewModel<SettingsScreenViewModel>()
  val state by viewModel.stateHolder.state.collectAsState()

  SettingsScreenStateless(
    state = state,
    navigateBack = navigateBack,
    onDnsRowClick = viewModel::onDnsRowClick,
    onDnsDialogConfirmClick = viewModel::onDnsSelected,
    onDnsDialogDismissClick = viewModel::onDnsDialogDismissClick,
  )
}

@Composable
fun SettingsScreenStateless(
  state: State,
  navigateBack: () -> Unit,
  onDnsRowClick: () -> Unit,
  onDnsDialogConfirmClick: (DdsConfigurator.Dns) -> Unit,
  onDnsDialogDismissClick: () -> Unit,
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
    }
    if (state.isDnsSelectorVisible) {
      DnsDialog(
        state = state,
        onConfirmClick = onDnsDialogConfirmClick,
        onDismissClick = onDnsDialogDismissClick,
        onDismissRequest = onDnsDialogDismissClick,
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
    modifier = modifier
      .heightIn(min = 60.dp)
      .padding(16.dp)
      .fillMaxWidth(),
  ) {
    Text(
      text = title,
      overflow = TextOverflow.Ellipsis,
      maxLines = 1,
      fontSize = 18.sp,
      color = BasedAppColor.TextPrimary,
      modifier = Modifier.weight(1f),
    )
    Text(
      text = value,
      textAlign = TextAlign.End,
      overflow = TextOverflow.Ellipsis,
      maxLines = 1,
      fontSize = 18.sp,
      color = BasedAppColor.TextSecondary,
      modifier = Modifier
        .weight(1f),

    )
  }
}

@Composable
private fun DnsDialog(
  state: State,
  onConfirmClick: (DdsConfigurator.Dns) -> Unit,
  onDismissClick: () -> Unit,
  onDismissRequest: () -> Unit = {},
) {
  var radioState by remember { mutableStateOf(state.currentDns) }
  AlertDialog(
    onDismissRequest = onDismissRequest,
    title = { Text(stringResource(R.string.settings_dns_change_title)) },
    text = {
      Column {
        state.dnsOptions.forEach { dns ->
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .selectable(
                selected = dns == radioState,
                onClick = { radioState = dns },
                role = Role.RadioButton,
              )
              .padding(vertical = 8.dp),
          ) {
            RadioButton(
              selected = dns == radioState,
              onClick = null,
              modifier = Modifier.padding(end = 8.dp),
            )
            Text(
              text = stringResource(dns.getLabelRes()),
              maxLines = 1,
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { radioState?.let(onConfirmClick) },
      ) { Text(stringResource(R.string.common_ok)) }
    },
    dismissButton = {
      Button(
        onClick = onDismissClick,
      ) {
        Text(stringResource(R.string.common_cancel))
      }
    },
  )
}

@Composable
@Preview
private fun DnsDialogPreview() {
  DnsDialog(
    state = State(
      currentDns = DdsConfigurator.Dns.Cloudflare,
    ),
    onConfirmClick = {},
    onDismissClick = {},
    onDismissRequest = {},
  )
}

fun DdsConfigurator.Dns.getLabelRes() =
  when (this) {
    DdsConfigurator.Dns.Cloudflare -> R.string.settings_dns_cloudflare
    DdsConfigurator.Dns.Google -> R.string.settings_dns_google
    DdsConfigurator.Dns.Handshake -> R.string.settings_dns_handshake
  }
