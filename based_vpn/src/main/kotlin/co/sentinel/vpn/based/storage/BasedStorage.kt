package co.sentinel.vpn.based.storage

import android.content.SharedPreferences
import co.sentinel.vpn.based.network.model.City
import co.sentinel.vpn.based.network.model.Country
import co.sentinel.vpn.based.network.model.Protocol
import co.sentinel.vpn.based.shared_preferences.delegate
import co.sentinel.vpn.based.shared_preferences.getValue
import co.sentinel.vpn.based.shared_preferences.setValue
import com.google.gson.Gson
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class BasedStorage
@Inject constructor(
  private val gson: Gson,
  prefs: SharedPreferences,
) {

  private var tokenPref: String by prefs.delegate("device_token", "")

  private val selectedCityDelegate = prefs.delegate("selected_city", "")
  private var selectedCityPref: String by selectedCityDelegate

  private var protocolPref by prefs.delegate("selected_protocol", "")

  private var onboardingPref by prefs.delegate("onboarding_shown", false)

  fun storeToken(token: String) {
    tokenPref = token
  }

  fun getToken(): String = tokenPref

  fun clearToken() {
    tokenPref = ""
  }

  fun storeSelectedCity(
    country: Country,
    city: City,
  ) {
    val selectedCity = SelectedCity(
      id = city.id,
      name = city.name,
      countryId = country.id,
      countryName = country.name,
      countryFlag = country.flag,
    )
    val selectedCityJson = gson.toJson(selectedCity)
    selectedCityPref = selectedCityJson
  }

  fun observeSelectedCity(): Flow<SelectedCity?> = selectedCityDelegate.observe
    .map { cityJson -> gson.fromJson(cityJson, SelectedCity::class.java) }
    .catch { emit(null) }

  fun storeVpnProtocol(protocol: Protocol) {
    protocolPref = protocol.strValue
  }

  fun getVpnProtocol(): Protocol = Protocol.fromString(protocolPref)

  fun isOnboardingShown() = onboardingPref

  fun onOnboardingShown() {
    onboardingPref = true
  }
}
