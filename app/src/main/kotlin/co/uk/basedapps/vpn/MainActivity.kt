package co.uk.basedapps.vpn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.core.view.WindowCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import co.uk.basedapps.vpn.ui.screens.cities.CitiesScreen
import co.uk.basedapps.vpn.ui.screens.countries.CountriesScreen
import co.uk.basedapps.vpn.ui.screens.dashboard.DashboardScreen
import co.uk.basedapps.vpn.ui.screens.settings.SettingsScreen
import co.uk.basedapps.vpn.ui.theme.BasedVPNTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setFullScreen()
    setContent {
      BasedVPNTheme {
        val navController = rememberNavController()
        NavHost(
          navController = navController,
          startDestination = Destination.Dashboard,
          enterTransition = { EnterTransition.None },
          exitTransition = { ExitTransition.None },
        ) {
          composable(Destination.Dashboard) {
            DashboardScreen(
              navigateToCountries = { navController.navigate(Destination.Countries) },
              navigateToSettings = { navController.navigate(Destination.Settings) },
            )
          }
          composable(Destination.Countries) {
            CountriesScreen(
              navigateBack = { navController.popBackStack() },
              navigateToCities = { country ->
                navController.navigate("countries/${country.id}/cities")
              },
            )
          }
          composable(
            route = Destination.Cities,
            arguments = listOf(navArgument(Args.CountryId) { type = NavType.IntType }),
          ) { backStackEntry ->
            CitiesScreen(
              countryId = backStackEntry.arguments?.getInt(Args.CountryId),
              navigateBack = { navController.popBackStack() },
              navigateBackToRoot = {
                navController.popBackStack(
                  route = Destination.Dashboard,
                  inclusive = false,
                )
              },
            )
          }
          composable(Destination.Settings) {
            SettingsScreen(
              navigateBack = { navController.popBackStack() },
            )
          }
        }
      }
    }
  }
}

fun ComponentActivity.setFullScreen() {
  WindowCompat.setDecorFitsSystemWindows(window, false)
}

object Destination {
  const val Dashboard = "dashboard"
  const val Countries = "countries"
  const val Cities = "countries/{${Args.CountryId}}/cities"
  const val Settings = "settings"
}

object Args {
  const val CountryId = "countryId"
}
