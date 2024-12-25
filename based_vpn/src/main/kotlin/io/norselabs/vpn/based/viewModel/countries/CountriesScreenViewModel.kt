package io.norselabs.vpn.based.viewModel.countries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.norselabs.vpn.based.network.model.Country
import io.norselabs.vpn.based.network.repository.AppRepository
import io.norselabs.vpn.common.state.Status
import io.norselabs.vpn.core_vpn.storage.CoreStorage
import io.norselabs.vpn.core_vpn.vpn.Protocol
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class CountriesScreenViewModel
@Inject constructor(
  val stateHolder: CountriesScreenStateHolder,
  private val repository: AppRepository,
  private val coreStorage: CoreStorage,
) : ViewModel() {

  private val state: CountriesScreenState
    get() = stateHolder.state.value

  init {
    stateHolder.updateState { copy(status = Status.Loading) }
    getCountries()
  }

  private fun getCountries() {
    viewModelScope.launch {
      val protocol = coreStorage.getVpnProtocol().takeIf { it != Protocol.NONE }
      val countries = repository.getCountries(protocol).getOrNull()?.data
      if (countries != null) {
        stateHolder.updateState {
          copy(
            status = Status.Data,
            countries = countries,
          )
        }
      } else {
        stateHolder.updateState { copy(status = Status.Error(false)) }
      }
    }
  }

  fun onCountryClick(country: Country) {
    stateHolder.sendEffect(CountriesScreenEffect.ShowCitiesScreen(country))
  }

  fun onTryAgainClick() {
    stateHolder.updateState { copy(status = Status.Error(true)) }
    getCountries()
  }
}
