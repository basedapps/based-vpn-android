package co.uk.basedapps.vpn.ui.screens.settings.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.uk.basedapps.vpn.R
import co.uk.basedapps.vpn.network.model.Protocol
import co.uk.basedapps.vpn.ui.theme.BasedAppColor
import co.uk.basedapps.vpn.viewModel.settings.SettingsScreenState

@Composable
fun ProtocolDialog(
  state: SettingsScreenState,
  onConfirmClick: (Protocol) -> Unit,
  onDismissClick: () -> Unit,
  onDismissRequest: () -> Unit = {},
) {
  var radioState by remember { mutableStateOf(state.currentProtocol) }
  AlertDialog(
    onDismissRequest = onDismissRequest,
    title = { Text(stringResource(R.string.settings_protocol_change_title)) },
    text = {
      Column {
        state.protocolOptions.forEach { protocol ->
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .selectable(
                selected = protocol == radioState,
                onClick = { radioState = protocol },
                role = Role.RadioButton,
              )
              .padding(vertical = 8.dp),
          ) {
            RadioButton(
              selected = protocol == radioState,
              onClick = null,
              colors = RadioButtonDefaults.colors(
                selectedColor = BasedAppColor.Accent,
              ),
              modifier = Modifier.padding(end = 8.dp),
            )
            Text(
              text = stringResource(protocol.labelRes),
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
private fun ProtocolDialogPreview() {
  ProtocolDialog(
    state = SettingsScreenState(
      currentProtocol = Protocol.WIREGUARD,
    ),
    onConfirmClick = {},
    onDismissClick = {},
    onDismissRequest = {},
  )
}
