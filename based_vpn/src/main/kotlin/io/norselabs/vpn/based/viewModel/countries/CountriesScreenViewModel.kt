package io.norselabs.vpn.based.viewModel.countries

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.norselabs.vpn.based.viewModel.countries.CountriesScreenEffect as Effect
import io.norselabs.vpn.common.state.Status
import io.norselabs.vpn.common_flags.mapToFlag
import io.norselabs.vpn.common_purchases.PurchasesManager
import io.norselabs.vpn.core_vpn.storage.CoreStorage
import io.norselabs.vpn.core_vpn.vpn.Destination
import io.norselabs.vpn.core_vpn.vpn.destination.DestinationStorage
import io.norselabs.vpn.sdk.dvpn_client.DVPNClient
import javax.inject.Inject
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CountriesScreenViewModel
@Inject constructor(
  val stateHolder: CountriesScreenStateHolder,
  private val dvpnClient: DVPNClient,
  private val coreStorage: CoreStorage,
  private val purchasesManager: PurchasesManager,
  private val destinationStorage: DestinationStorage,
) : ScreenModel {

  private val state: CountriesScreenState
    get() = stateHolder.state.value

  private var fetchDataJob: Job? = null

  fun fetchData() {
    fetchDataJob?.cancel()
    fetchDataJob = screenModelScope.launch {
      val isSubscribed = purchasesManager.getCustomerData()
        .getOrNull()?.isSubscribed == true
      val wasSubscribed = state.isSubscribed
      stateHolder.updateState {
        copy(isSubscribed = isSubscribed)
      }
      val isFresh = wasSubscribed != isSubscribed || state.isRefreshing
      getCountries(isFresh)
    }
  }

  private suspend fun getCountries(isFresh: Boolean) {
    val protocol = coreStorage.getVpnProtocol()
    val countries = dvpnClient.getCountries(protocol = protocol?.strValue, isFresh = isFresh).getOrNull()
    if (countries != null) {
      stateHolder.updateState {
        copy(
          status = Status.Data,
          countries = countries.map { country ->
            CountryUi(
              id = country.id,
              name = country.name,
              code = country.code,
              flag = mapToFlag(country.code),
              serversAvailable = country.serversAvailable,
            )
          }.toPersistentList(),
          isRefreshing = false,
        )
      }
    } else {
      stateHolder.updateState {
        copy(
          status = Status.Error(false),
          isRefreshing = false,
        )
      }
    }
  }

  fun onQuickConnectClick() {
    destinationStorage.storeDestination(Destination.Random)
    stateHolder.sendEffect(Effect.GoBack)
  }

  fun onCountryClick(country: CountryUi) {
    stateHolder.sendEffect(Effect.ShowCitiesScreen(country.id))
  }

  fun onBackClick() {
    stateHolder.sendEffect(Effect.GoBack)
  }

  fun onTryAgainClick() {
    stateHolder.updateState { copy(status = Status.Error(true)) }
    fetchData()
  }

  fun onSubscribeClick() {
    stateHolder.sendEffect(Effect.ShowPurchaseScreen)
  }

  fun onPull2Refresh() {
    if (state.isRefreshing) return
    stateHolder.updateState { copy(isRefreshing = true) }
    fetchData()
  }
}
