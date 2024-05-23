package co.uk.basedapps.vpn.viewModel.countries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.uk.basedapps.vpn.common.state.Status
import co.uk.basedapps.vpn.network.repository.BasedRepository
import co.uk.basedapps.vpn.network.model.Country
import co.uk.basedapps.vpn.network.model.Protocol
import co.uk.basedapps.vpn.storage.BasedStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class CountriesScreenViewModel
@Inject constructor(
  val stateHolder: CountriesScreenStateHolder,
  private val repository: BasedRepository,
  private val storage: BasedStorage,
) : ViewModel() {

  private val state: CountriesScreenState
    get() = stateHolder.state.value

  init {
    stateHolder.updateState { copy(status = Status.Loading) }
    getCountries()
  }

  private fun getCountries() {
    viewModelScope.launch {
      val protocol = storage.getVpnProtocol().takeIf { it != Protocol.NONE }
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
