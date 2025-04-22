package co.uk.basedapps.vpn.ui.screens.countries

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.hilt.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import co.uk.basedapps.vpn.R
import co.uk.basedapps.vpn.ui.screens.cities.CitiesScreen
import co.uk.basedapps.vpn.ui.theme.BasedAppColor
import co.uk.basedapps.vpn.ui.widget.ErrorScreen
import co.uk.basedapps.vpn.ui.widget.TopBar
import io.norselabs.vpn.based.compose.EffectHandler
import io.norselabs.vpn.based.viewModel.countries.CountriesScreenEffect as Effect
import io.norselabs.vpn.based.viewModel.countries.CountriesScreenState as State
import io.norselabs.vpn.based.viewModel.countries.CountriesScreenViewModel
import io.norselabs.vpn.based.viewModel.countries.CountryUi
import io.norselabs.vpn.common.state.Status
import io.norselabs.vpn.common_compose.NavigationSpacer

class CountriesScreen : Screen {

  @Composable
  override fun Content() {
    val viewModel = getScreenModel<CountriesScreenViewModel>()
    val state by viewModel.stateHolder.state.collectAsState()

    val navigator = LocalNavigator.currentOrThrow

    EffectHandler(viewModel.stateHolder.effects) { effect ->
      when (effect) {
        is Effect.GoBack -> navigator.pop()

        is Effect.ShowCitiesScreen ->
          navigator.push(CitiesScreen(effect.countryId))

        is Effect.ShowPurchaseScreen -> Unit
      }
    }

    LaunchedEffect(Unit) {
      viewModel.fetchData()
    }

    CountriesScreenStateless(
      state = state,
      navigateBack = viewModel::onBackClick,
      onItemClick = viewModel::onCountryClick,
      onPull2Refresh = viewModel::onPull2Refresh,
      onTryAgainClick = viewModel::onTryAgainClick,
    )
  }
}

@Composable
fun CountriesScreenStateless(
  state: State,
  navigateBack: () -> Unit,
  onItemClick: (CountryUi) -> Unit,
  onPull2Refresh: () -> Unit,
  onTryAgainClick: () -> Unit,
) {
  Scaffold(
    containerColor = BasedAppColor.Background,
    topBar = {
      TopBar(
        title = stringResource(R.string.countries_title),
        navigateBack = navigateBack,
      )
    },
    content = { paddingValues ->
      Content(
        paddingValues = paddingValues,
        state = state,
        onItemClick = onItemClick,
        onPull2Refresh = onPull2Refresh,
        onTryAgainClick = onTryAgainClick,
      )
    },
  )
}

@Composable
fun Content(
  paddingValues: PaddingValues,
  state: State,
  onItemClick: (CountryUi) -> Unit,
  onPull2Refresh: () -> Unit,
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
        onPull2Refresh = onPull2Refresh,
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Data(
  state: State,
  onItemClick: (CountryUi) -> Unit,
  onPull2Refresh: () -> Unit,
) {
  PullToRefreshBox(
    isRefreshing = state.isRefreshing,
    onRefresh = onPull2Refresh,
    modifier = Modifier.fillMaxSize(),
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
    ) {
      items(state.countries) { country ->
        CountryRow(country, onItemClick)
      }
      item {
        NavigationSpacer()
      }
    }
  }
}

@Composable
private fun CountryRow(
  country: CountryUi,
  onItemClick: (CountryUi) -> Unit,
) {
  Row(
    modifier = Modifier
      .clickable(onClick = { onItemClick(country) })
      .heightIn(min = 60.dp)
      .padding(16.dp)
      .fillMaxWidth(),
  ) {
    val flagRes = country.flag?.res
    if (flagRes != null) {
      Image(
        painter = painterResource(flagRes),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .size(width = 36.dp, height = 24.dp)
          .clip(RoundedCornerShape(4.dp)),
      )
    } else {
      Box(
        modifier = Modifier
          .background(BasedAppColor.Divider)
          .size(width = 36.dp, height = 24.dp)
          .clip(RoundedCornerShape(4.dp)),
      )
    }
    Spacer(modifier = Modifier.size(16.dp))
    Text(
      text = country.name,
      overflow = TextOverflow.Ellipsis,
      maxLines = 1,
      fontSize = 18.sp,
    )
  }
}
