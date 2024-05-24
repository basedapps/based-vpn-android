package co.sentinel.vpn.based.viewModel.cities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.sentinel.vpn.based.network.model.City
import co.sentinel.vpn.based.network.model.Protocol
import co.sentinel.vpn.based.network.repository.BasedRepository
import co.sentinel.vpn.based.state.Status
import co.sentinel.vpn.based.storage.BasedStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class CitiesScreenViewModel
@Inject constructor(
  val stateHolder: CitiesScreenStateHolder,
  private val repository: BasedRepository,
  private val storage: BasedStorage,
) : ViewModel() {

  private val state: CitiesScreenState
    get() = stateHolder.state.value

  fun setCountryId(countryId: Int?) {
    if (countryId != null) {
      stateHolder.updateState {
        copy(
          status = Status.Loading,
          countryId = countryId,
        )
      }
      getCities(countryId)
    }
  }

  private fun getCities(countryId: Int) {
    viewModelScope.launch {
      val protocol = storage.getVpnProtocol().takeIf { it != Protocol.NONE }
      val countries = repository.getCountries(protocol).getOrNull()?.data // todo: replace with cache
      val cities = repository.getCities(countryId, protocol).getOrNull()?.data
      if (countries != null && cities != null) {
        stateHolder.updateState {
          copy(
            status = Status.Data,
            country = countries.first { it.id == countryId },
            cities = cities,
          )
        }
      } else {
        stateHolder.updateState { copy(status = Status.Error(false)) }
      }
    }
  }

  fun onCityClick(city: City) {
    val country = state.country ?: return
    storage.storeSelectedCity(country, city)
    stateHolder.sendEffect(CitiesScreenEffect.GoBackToRoot)
  }

  fun onTryAgainClick() {
    stateHolder.updateState { copy(status = Status.Error(true)) }
    state.countryId?.let(::getCities)
  }
}
