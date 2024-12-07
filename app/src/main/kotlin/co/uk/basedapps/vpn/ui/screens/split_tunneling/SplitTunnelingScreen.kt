package co.uk.basedapps.vpn.ui.screens.split_tunneling

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import co.sentinel.vpn.based.compose.EffectHandler
import co.sentinel.vpn.based.viewModel.split_tunneling.NetworkApp
import co.sentinel.vpn.based.viewModel.split_tunneling.SplitTunnelingScreenEffect
import co.sentinel.vpn.based.viewModel.split_tunneling.SplitTunnelingScreenState as State
import co.sentinel.vpn.based.viewModel.split_tunneling.SplitTunnelingScreenViewModel
import co.uk.basedapps.vpn.R
import co.uk.basedapps.vpn.ui.theme.BasedAppColor
import co.uk.basedapps.vpn.ui.widget.TopBar
import coil.compose.AsyncImage

@Composable
fun SplitTunnelingScreenScreen(
  navigateBack: () -> Unit,
) {

  val viewModel = hiltViewModel<SplitTunnelingScreenViewModel>()
  val state by viewModel.stateHolder.state.collectAsState()

  EffectHandler(viewModel.stateHolder.effects) { effect ->
    when (effect) {
      is SplitTunnelingScreenEffect.Temp -> Unit
    }
  }

  SplitTunnelingScreenStateless(
    state = state,
    navigateBack = navigateBack,
    onSplitTunnelingEnabled = viewModel::onSplitTunnelingEnabled,
    onBypassModeEnabled = viewModel::onBypassModeEnabled,
    onAppChecked = viewModel::onAppChecked,
  )
}

@Composable
private fun SplitTunnelingScreenStateless(
  state: State,
  navigateBack: () -> Unit,
  onSplitTunnelingEnabled: (Boolean) -> Unit,
  onBypassModeEnabled: (Boolean) -> Unit,
  onAppChecked: (NetworkApp, Boolean) -> Unit,
) {
  Scaffold(
    containerColor = BasedAppColor.Background,
    topBar = {
      TopBar(
        title = stringResource(R.string.split_tunneling_title),
        navigateBack = navigateBack,
      )
    },
    content = { paddingValues ->
      Column(
        modifier = Modifier
          .padding(paddingValues)
          .fillMaxSize(),
      ) {
        Row(
          modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        ) {
          SwitchRow(
            text = stringResource(R.string.split_tunneling_selector),
            checked = state.isSplitTunnelingEnabled,
            onCheckedChange = onSplitTunnelingEnabled,
            modifier = Modifier.weight(1f),
          )
          Spacer(modifier = Modifier.size(16.dp))
          SwitchRow(
            text = stringResource(R.string.split_tunneling_bypass_selector),
            checked = state.isBypassModeEnabled,
            onCheckedChange = onBypassModeEnabled,
            modifier = Modifier.weight(1f),
          )
        }
        LazyColumn(
          modifier = Modifier.weight(1f),
        ) {
          items(
            items = state.applications,
            key = { item -> item.packageName },
          ) { app ->
            HorizontalDivider(color = BasedAppColor.Divider)
            AppRow(app, onAppChecked)
          }
        }
      }
    },
  )
}

@Composable
private fun AppRow(
  app: NetworkApp,
  onCheck: (NetworkApp, Boolean) -> Unit,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  ) {
    AsyncImage(
      model = app.appIcon,
      contentDescription = null,
      modifier = Modifier.size(32.dp),
    )
    Spacer(modifier = Modifier.size(8.dp))
    Column(
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.weight(1f),
    ) {
      Text(
        text = app.appName,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        fontSize = 16.sp,
        color = BasedAppColor.TextPrimary,
        modifier = Modifier.fillMaxWidth(),
      )
      Text(
        text = app.packageName,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        fontSize = 12.sp,
        lineHeight = 14.sp,
        color = BasedAppColor.TextSecondary,
        modifier = Modifier.fillMaxWidth(),
      )
    }
    Checkbox(
      checked = app.isChecked,
      onCheckedChange = { isChecked -> onCheck(app, isChecked) },
    )
  }
}

@Composable
private fun SwitchRow(
  text: String,
  checked: Boolean,
  onCheckedChange: ((Boolean) -> Unit)?,
  modifier: Modifier = Modifier,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier,
  ) {
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
    )
    Spacer(modifier = Modifier.size(8.dp))
    Text(
      text = text,
      overflow = TextOverflow.Ellipsis,
      maxLines = 1,
      fontSize = 18.sp,
      color = BasedAppColor.TextPrimary,
    )
  }
}
