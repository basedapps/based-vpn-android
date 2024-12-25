package co.uk.basedapps.vpn.ui.screens.dashboard

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import co.uk.basedapps.vpn.R
import co.uk.basedapps.vpn.ui.theme.BasedAppColor
import co.uk.basedapps.vpn.ui.theme.BasedVPNTheme
import co.uk.basedapps.vpn.ui.widget.BasedAlertDialog
import co.uk.basedapps.vpn.ui.widget.BasedButton
import co.uk.basedapps.vpn.ui.widget.ButtonStyle
import co.uk.basedapps.vpn.ui.widget.ErrorScreen
import io.norselabs.vpn.based.compose.EffectHandler
import io.norselabs.vpn.based.network.model.IpData
import io.norselabs.vpn.based.viewModel.dashboard.DashboardScreenEffect as Effect
import io.norselabs.vpn.based.viewModel.dashboard.DashboardScreenState as State
import io.norselabs.vpn.based.viewModel.dashboard.DashboardScreenViewModel
import io.norselabs.vpn.based.viewModel.dashboard.RatingClick
import io.norselabs.vpn.based.viewModel.dashboard.VpnStatus
import io.norselabs.vpn.based.vpn.getVpnPermissionRequest
import io.norselabs.vpn.common.ext.goToGooglePlay
import io.norselabs.vpn.common.ext.mailTo
import io.norselabs.vpn.common.state.Status
import io.norselabs.vpn.common_flags.mapToFlag
import io.norselabs.vpn.common_map.WorldMap
import io.norselabs.vpn.core_vpn.user.UserStatus
import io.norselabs.vpn.core_vpn.vpn.Destination
import timber.log.Timber

@Composable
fun DashboardScreen(
  navigateToCountries: () -> Unit,
  navigateToSettings: () -> Unit,
) {
  val viewModel = hiltViewModel<DashboardScreenViewModel>()
  val state by viewModel.stateHolder.state.collectAsState()

  val context = LocalContext.current

  val vpnPermissionRequest = rememberLauncherForActivityResult(
    ActivityResultContracts.StartActivityForResult(),
  ) { result ->
    viewModel.onPermissionsResult(result.resultCode == Activity.RESULT_OK)
  }

  EffectHandler(viewModel.stateHolder.effects) { effect ->
    when (effect) {
      is Effect.ShowAd -> viewModel.onAdShown()

      is Effect.ShowSelectServer -> navigateToCountries()

      is Effect.CheckVpnPermission -> {
        val intent = getVpnPermissionRequest(context)
        if (intent != null) {
          vpnPermissionRequest.launch(intent)
        } else {
          viewModel.onPermissionsResult(true)
        }
      }

      is Effect.ShowSettings -> navigateToSettings()

      is Effect.ShowGooglePlay -> context.goToGooglePlay()

      is Effect.EmailToSupport -> context.mailTo("hello@world.com")

      is Effect.ShowRating -> {
        Timber.tag("DashboardScreenEffect").d("ShowRating")
      }
    }
  }

  DashboardScreenStateless(
    state = state,
    onConnectClick = viewModel::onConnectClick,
    onDisconnectClick = viewModel::onDisconnectClick,
    onQuickConnectClick = viewModel::onQuickConnectClick,
    onSelectServerClick = viewModel::onSelectServerClick,
    onSettingsClick = viewModel::onSettingsClick,
    onTryAgainClick = viewModel::onTryAgainClick,
    onUpdateClick = viewModel::onUpdateClick,
    onAlertConfirmClick = viewModel::onAlertConfirmClick,
    onAlertDismissRequest = viewModel::onAlertDismissRequest,
    onRatingClick = viewModel::onRatingClick,
  )
}

@Composable
fun DashboardScreenStateless(
  state: State,
  onConnectClick: () -> Unit,
  onDisconnectClick: () -> Unit,
  onQuickConnectClick: () -> Unit,
  onSelectServerClick: () -> Unit,
  onSettingsClick: () -> Unit,
  onTryAgainClick: () -> Unit,
  onUpdateClick: () -> Unit,
  onAlertConfirmClick: () -> Unit,
  onAlertDismissRequest: () -> Unit,
  onRatingClick: (RatingClick) -> Unit,
) {
  when (state.status) {
    is Status.Error -> {
      when (state.userStatus) {
        UserStatus.VersionOutdated -> {
          ErrorScreen(
            title = stringResource(R.string.update_required_title),
            description = stringResource(R.string.update_required_description),
            buttonLabel = stringResource(R.string.update_required_button),
            imageResId = R.drawable.ic_update,
            onButtonClick = onUpdateClick,
          )
        }

        UserStatus.Banned -> {
          ErrorScreen(
            title = null,
            description = stringResource(R.string.error_banned_title),
            onButtonClick = null,
          )
        }

        else -> {
          ErrorScreen(
            isLoading = (state.status as Status.Error).isLoading,
            onButtonClick = onTryAgainClick,
          )
        }
      }
    }

    else -> Content(
      state = state,
      onConnectClick = onConnectClick,
      onDisconnectClick = onDisconnectClick,
      onQuickConnectClick = onQuickConnectClick,
      onSelectServerClick = onSelectServerClick,
      onSettingsClick = onSettingsClick,
      onAlertConfirmClick = onAlertConfirmClick,
      onAlertDismissRequest = onAlertDismissRequest,
      onRatingClick = onRatingClick,
    )
  }
}

@Composable
private fun Content(
  state: State,
  onConnectClick: () -> Unit,
  onDisconnectClick: () -> Unit,
  onQuickConnectClick: () -> Unit,
  onSelectServerClick: () -> Unit,
  onSettingsClick: () -> Unit,
  onAlertConfirmClick: () -> Unit,
  onAlertDismissRequest: () -> Unit,
  onRatingClick: (RatingClick) -> Unit,
) {
  Box(
    modifier = Modifier
      .fillMaxSize(),
  ) {
    Column {
      TopBar(
        state = state,
        onSettingsClick = onSettingsClick,
      )
      WorldMap(
        lat = state.ipData?.latitude ?: 0.0,
        long = state.ipData?.longitude ?: 0.0,
        color = when (state.vpnStatus) {
          is VpnStatus.Connected -> Color(0xFF09D1BC)
          else -> Color(0xFFF0A83E)
        },
        modifier = Modifier.weight(1f),
      )
      BottomBar(
        state = state,
        onConnectClick = onConnectClick,
        onDisconnectClick = onDisconnectClick,
        onQuickConnectClick = onQuickConnectClick,
        onSelectServerClick = onSelectServerClick,
      )
    }
    if (state.status is Status.Loading) {
      LoadingOverlay()
    }
    if (state.isErrorAlertVisible) {
      BasedAlertDialog(
        title = stringResource(R.string.dashboard_error_connection_title),
        description = stringResource(R.string.dashboard_error_connection_description),
        onConfirmClick = onAlertConfirmClick,
        onDismissRequest = onAlertDismissRequest,
      )
    }
    if (state.isRatingAlertVisible) {
      BasedAlertDialog(
        title = stringResource(R.string.dashboard_rating),
        onConfirmClick = { onRatingClick(RatingClick.Positive) },
        onDismissClick = { onRatingClick(RatingClick.Negative) },
        onDismissRequest = { onRatingClick(RatingClick.Dismiss) },
      )
    }
  }
}

@Composable
private fun LoadingOverlay() {
  Box(
    contentAlignment = Alignment.Center,
    modifier = Modifier
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = {},
      )
      .navigationBarsPadding()
      .background(Color.Black.copy(alpha = 0.5f))
      .fillMaxSize(),
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      CircularProgressIndicator(
        color = Color.White,
      )
      Spacer(modifier = Modifier.size(24.dp))
      Text(
        text = stringResource(R.string.dashboard_loading),
        fontSize = 16.sp,
        color = Color.White,
        textAlign = TextAlign.Center,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 48.dp),
      )
    }
  }
}

@Composable
private fun TopBar(
  state: State,
  onSettingsClick: () -> Unit,
) {
  Card(
    shape = RoundedCornerShape(
      bottomStart = 16.dp,
      bottomEnd = 16.dp,
    ),
    colors = CardDefaults.cardColors(containerColor = Color.White),
  ) {
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .statusBarsPadding()
        .padding(top = 8.dp, bottom = 24.dp)
        .padding(horizontal = 16.dp)
        .fillMaxWidth(),
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 50.dp),
      ) {
        Text(
          text = stringResource(R.string.dashboard_your_ip).uppercase(),
          color = BasedAppColor.TextSecondary,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
        )
        Text(
          text = state.ipData?.ip.orEmpty(),
          color = BasedAppColor.TextPrimary,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          overflow = TextOverflow.Ellipsis,
          maxLines = 1,
        )
      }
      Button(
        colors = ButtonDefaults.buttonColors(
          containerColor = BasedAppColor.ButtonTertiary,
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(0.dp),
        onClick = onSettingsClick,
        modifier = Modifier
          .size(44.dp)
          .align(Alignment.CenterEnd),
      ) {
        Icon(
          painter = painterResource(R.drawable.ic_settings),
          contentDescription = stringResource(R.string.dashboard_menu_settings),
          modifier = Modifier.size(24.dp),
          tint = BasedAppColor.ButtonTertiaryIcon,
        )
      }
    }
  }
}

@Composable
fun BottomBar(
  state: State,
  onConnectClick: () -> Unit,
  onDisconnectClick: () -> Unit,
  onQuickConnectClick: () -> Unit,
  onSelectServerClick: () -> Unit,
) {
  Card(
    shape = RoundedCornerShape(
      topStart = 16.dp,
      topEnd = 16.dp,
    ),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .padding(top = 24.dp, bottom = 16.dp)
        .navigationBarsPadding(),
    ) {
      val destination = state.destination as? Destination.City
      if (destination != null) {
        SelectedCityRow(
          destination = destination,
          onClick = onSelectServerClick,
        )
        Spacer(modifier = Modifier.size(16.dp))
      }
      Row {
        BasedButton(
          text = stringResource(
            when (state.vpnStatus) {
              is VpnStatus.Connected -> R.string.dashboard_disconnect_from_vpn
              else -> R.string.dashboard_connect_to_vpn
            },
          ),
          style = when (state.vpnStatus) {
            is VpnStatus.Connected -> ButtonStyle.Secondary
            else -> ButtonStyle.Primary
          },
          isLoading = when (state.vpnStatus) {
            is VpnStatus.Connecting -> true
            else -> false
          },
          onClick = {
            if (state.vpnStatus is VpnStatus.Disconnected) {
              onConnectClick()
            } else {
              onDisconnectClick()
            }
          },
          modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.size(8.dp))
        if (state.vpnStatus == VpnStatus.Disconnected) {
          QuickConnectButton(onQuickConnectClick)
        }
      }
    }
  }
}

@Composable
private fun QuickConnectButton(onClick: () -> Unit) {
  Button(
    onClick = onClick,
    colors = ButtonDefaults.buttonColors(
      containerColor = BasedAppColor.ButtonPrimary,
      contentColor = BasedAppColor.ButtonPrimaryText,
    ),
    shape = RoundedCornerShape(8.dp),
    contentPadding = PaddingValues(),
    modifier = Modifier.size(60.dp),
  ) {
    Icon(
      painter = painterResource(R.drawable.ic_flash),
      contentDescription = null,
      modifier = Modifier.size(32.dp),
    )
  }
}

@Composable
fun SelectedCityRow(
  destination: Destination.City,
  onClick: () -> Unit,
) {
  Row(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .clickable(onClick = onClick)
      .heightIn(min = 60.dp)
      .background(BasedAppColor.Elevation)
      .padding(16.dp)
      .fillMaxWidth(),
  ) {
    val flagRes = mapToFlag(destination.countryCode)?.res
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
      text = buildAnnotatedString {
        withStyle(style = SpanStyle(BasedAppColor.TextPrimary)) {
          append(destination.countryName)
        }
        withStyle(style = SpanStyle(BasedAppColor.TextSecondary)) {
          append(" • ")
          append(destination.cityName)
        }
      },
      overflow = TextOverflow.Ellipsis,
      maxLines = 1,
      fontSize = 18.sp,
    )
  }
}

@Preview
@Composable
fun DashboardScreenPreview() {
  BasedVPNTheme {
    DashboardScreenStateless(
      state = State(
        destination = Destination.City(
          cityId = "",
          cityName = "Buenos Aires",
          countryId = "",
          countryName = "Argentina",
          countryCode = "AR",
        ),
        ipData = IpData(
          ip = "91.208.132.23",
          latitude = 0.0,
          longitude = 0.0,
        ),
      ),
      onConnectClick = {},
      onDisconnectClick = {},
      onQuickConnectClick = {},
      onSelectServerClick = {},
      onSettingsClick = {},
      onTryAgainClick = {},
      onUpdateClick = {},
      onAlertConfirmClick = {},
      onAlertDismissRequest = {},
      onRatingClick = {},
    )
  }
}
