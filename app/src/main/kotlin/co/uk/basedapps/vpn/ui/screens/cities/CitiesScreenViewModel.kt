package co.uk.basedapps.vpn.ui.screens.cities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.uk.basedapps.domain.functional.getOrNull
import co.uk.basedapps.vpn.common.Status
import co.uk.basedapps.vpn.network.BasedRepository
import co.uk.basedapps.vpn.network.model.City
import co.uk.basedapps.vpn.storage.BasedStorage
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
      val countries = repository.getCountries().getOrNull()?.data // todo: replace with cache
      val cities = repository.getCities(countryId).getOrNull()?.data
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
