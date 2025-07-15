package co.uk.basedapps.vpn.ui.screens.servers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import co.uk.basedapps.vpn.ui.widget.ErrorScreen
import co.uk.basedapps.vpn.ui.widget.RandomServerRow
import co.uk.basedapps.vpn.ui.widget.TopBar
import io.norselabs.vpn.based.viewModel.servers.ServerUi
import io.norselabs.vpn.based.viewModel.servers.ServersScreenEffect as Effect
import io.norselabs.vpn.based.viewModel.servers.ServersScreenState as State
import io.norselabs.vpn.based.viewModel.servers.ServersScreenViewModel
import io.norselabs.vpn.common.state.Status
import io.norselabs.vpn.common_compose.EffectHandler

class ServersScreen(
  private val countryId: String,
  private val cityId: String,
) : Screen {

  @Composable
  override fun Content() {
    val viewModel = getScreenModel<ServersScreenViewModel>()
    val state by viewModel.stateHolder.state.collectAsState()

    val navigator = LocalNavigator.currentOrThrow

    LaunchedEffect(countryId) {
      viewModel.setData(countryId = countryId, cityId = cityId)
    }

    EffectHandler(viewModel.stateHolder.effects) { effect ->
      when (effect) {
        is Effect.GoBack -> navigator.pop()

        is Effect.GoBackToRoot -> navigator.popUntilRoot()
      }
    }

    ServersScreenStateless(
      state = state,
      navigateBack = viewModel::onBackClick,
      onItemClick = viewModel::onServerClick,
      onQuickConnectClick = viewModel::onQuickConnectClick,
      onTryAgainClick = viewModel::onTryAgainClick,
    )
  }
}

@Composable
fun ServersScreenStateless(
  state: State,
  navigateBack: () -> Unit,
  onItemClick: (ServerUi) -> Unit,
  onQuickConnectClick: () -> Unit,
  onTryAgainClick: () -> Unit,
) {
  Scaffold(
    containerColor = BasedAppColor.Background,
    topBar = {
      TopBar(
        title = stringResource(R.string.servers_title),
        navigateBack = navigateBack,
      )
    },
    content = { paddingValues ->
      Content(
        paddingValues = paddingValues,
        state = state,
        onItemClick = onItemClick,
        onQuickConnectClick = onQuickConnectClick,
        onTryAgainClick = onTryAgainClick,
      )
    },
  )
}

@Composable
private fun Content(
  paddingValues: PaddingValues,
  state: State,
  onItemClick: (ServerUi) -> Unit,
  onQuickConnectClick: () -> Unit,
  onTryAgainClick: () -> Unit,
) {
  Box(
    modifier = Modifier
      .padding(paddingValues)
      .fillMaxSize(),
  ) {
    when (state.status) {
      is Status.Loading -> Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
      ) {
        CircularProgressIndicator()
      }

      is Status.Error -> ErrorScreen(
        isLoading = (state.status as Status.Error).isLoading,
        onButtonClick = onTryAgainClick,
      )

      is Status.Data -> Data(
        state = state,
        onItemClick = onItemClick,
        onQuickConnectClick = onQuickConnectClick,
      )
    }
  }
}

@Composable
private fun Data(
  state: State,
  onItemClick: (ServerUi) -> Unit,
  onQuickConnectClick: () -> Unit,
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
  ) {
    item {
      RandomServerRow(onClick = onQuickConnectClick)
      HorizontalDivider(color = BasedAppColor.Divider)
    }
    items(state.servers) { country ->
      ServerRow(country, onItemClick)
      HorizontalDivider(color = BasedAppColor.Divider)
    }
  }
}

@Composable
private fun ServerRow(
  server: ServerUi,
  onItemClick: (ServerUi) -> Unit,
) {
  Row(
    modifier = Modifier
      .clickable(onClick = { onItemClick(server) })
      .heightIn(min = 60.dp)
      .padding(16.dp)
      .fillMaxWidth(),
  ) {
    Text(
      text = server.name,
      overflow = TextOverflow.Ellipsis,
      maxLines = 1,
      fontSize = 18.sp,
    )
  }
}
