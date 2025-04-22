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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.hilt.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import co.uk.basedapps.vpn.R
import co.uk.basedapps.vpn.ui.theme.BasedAppColor
import co.uk.basedapps.vpn.ui.widget.Dropdown
import co.uk.basedapps.vpn.ui.widget.TopBar
import coil.compose.AsyncImage
import io.norselabs.vpn.based.viewModel.split_tunneling.NetworkApp
import io.norselabs.vpn.based.viewModel.split_tunneling.SplitTunnelingScreenEffect as Effect
import io.norselabs.vpn.based.viewModel.split_tunneling.SplitTunnelingScreenState as State
import io.norselabs.vpn.based.viewModel.split_tunneling.SplitTunnelingScreenViewModel
import io.norselabs.vpn.common_compose.EffectHandler
import io.norselabs.vpn.core_vpn.vpn.split_tunneling.SplitTunnelingStatus
import kotlinx.collections.immutable.persistentListOf

class SplitTunnelingScreen : Screen {

  @Composable
  override fun Content() {
    val viewModel = getScreenModel<SplitTunnelingScreenViewModel>()
    val state by viewModel.stateHolder.state.collectAsState()

    val navigator = LocalNavigator.currentOrThrow

    EffectHandler(viewModel.stateHolder.effects) { effect ->
      when (effect) {
        is Effect.GoBack -> navigator.pop()
      }
    }

    SplitTunnelingScreenStateless(
      state = state,
      navigateBack = viewModel::onBackClick,
      setSplitTunnelingStatus = viewModel::setSplitTunnelingStatus,
      onAppChecked = viewModel::onAppChecked,
    )
  }
}

@Composable
private fun SplitTunnelingScreenStateless(
  state: State,
  navigateBack: () -> Unit,
  setSplitTunnelingStatus: (SplitTunnelingStatus) -> Unit,
  onAppChecked: (NetworkApp, Boolean) -> Unit,
) {
  val context = LocalContext.current
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
        Dropdown(
          label = stringResource(R.string.split_tunneling_selector),
          items = remember {
            persistentListOf(
              SplitTunnelingStatus.Disabled,
              SplitTunnelingStatus.Enabled,
              SplitTunnelingStatus.Bypass,
            )
          },
          selected = state.status,
          mapItemLabel = { status ->
            context.getString(
              when (status) {
                SplitTunnelingStatus.Disabled -> R.string.split_tunneling_disabled
                SplitTunnelingStatus.Enabled -> R.string.split_tunneling_enabled
                SplitTunnelingStatus.Bypass -> R.string.split_tunneling_bypass
              },
            )
          },
          onSelect = setSplitTunnelingStatus,
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        )
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
