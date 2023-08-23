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
import androidx.compose.material3.Divider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import co.uk.basedapps.vpn.common.EffectHandler
import co.uk.basedapps.vpn.common.Status
import co.uk.basedapps.vpn.network.model.Country
import co.uk.basedapps.vpn.ui.screens.countries.CountriesScreenEffect as Effect
import co.uk.basedapps.vpn.ui.screens.countries.CountriesScreenState as State
import co.uk.basedapps.vpn.ui.theme.BasedAppColor
import co.uk.basedapps.vpn.ui.widget.ErrorScreen
import co.uk.basedapps.vpn.ui.widget.TopBar

@Composable
fun CountriesScreen(
  navigateBack: () -> Unit,
  navigateToCities: (Country) -> Unit,
) {
  val viewModel = hiltViewModel<CountriesScreenViewModel>()
  val state by viewModel.stateHolder.state.collectAsState()

  EffectHandler(viewModel.stateHolder.effects) { effect ->
    when (effect) {
      is Effect.ShowCitiesScreen ->
        navigateToCities(effect.country)
    }
  }

  CountriesScreenStateless(
    state = state,
    navigateBack = navigateBack,
    onItemClick = viewModel::onCountryClick,
    onTryAgainClick = viewModel::onTryAgainClick,
  )
}

@Composable
fun CountriesScreenStateless(
  state: State,
  navigateBack: () -> Unit,
  onItemClick: (Country) -> Unit,
  onTryAgainClick: () -> Unit,
) {
  Scaffold(
    containerColor = BasedAppColor.Background,
    topBar = {
      TopBar(
        title = "Choose country",
        navigateBack = navigateBack,
      )
    },
    content = { paddingValues ->
      Content(
        paddingValues = paddingValues,
        state = state,
        onItemClick = onItemClick,
        onTryAgainClick = onTryAgainClick,
      )
    },
  )
}

@Composable
fun Content(
  paddingValues: PaddingValues,
  state: State,
  onItemClick: (Country) -> Unit,
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
        isLoading = state.status.isLoading,
        onButtonClick = onTryAgainClick,
      )

      is Status.Data -> Data(state, onItemClick)
    }
  }
}

@Composable
private fun Data(
  state: State,
  onItemClick: (Country) -> Unit,
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
  ) {
    items(state.countries) { country ->
      CountryRow(country, onItemClick)
      Divider(color = BasedAppColor.Divider)
    }
  }
}

@Composable
private fun CountryRow(
  country: Country,
  onItemClick: (Country) -> Unit,
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
