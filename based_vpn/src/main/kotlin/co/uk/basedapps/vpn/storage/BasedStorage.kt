package co.uk.basedapps.vpn.storage

import android.content.SharedPreferences
import co.uk.basedapps.vpn.network.model.City
import co.uk.basedapps.vpn.network.model.Country
import co.uk.basedapps.vpn.network.model.Protocol
import co.uk.basedapps.vpn.prefs.delegate
import co.uk.basedapps.vpn.prefs.getValue
import co.uk.basedapps.vpn.prefs.setValue
import com.google.gson.Gson
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.json.JSONObject

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

  fun storeSelectedCity(country: Country, city: City) {
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

  fun observeSelectedCity(): Flow<SelectedCity?> =
    selectedCityDelegate.observe
      .map(::parseCity)
      .catch { emit(null) }

  private fun parseCity(cityJson: String): SelectedCity? {
    // make sure that the country json object is valid
    return if (JSONObject(cityJson).has("countryName")) {
      gson.fromJson(cityJson, SelectedCity::class.java)
    } else {
      return null
    }
  }

  fun storeVpnProtocol(protocol: Protocol) {
    protocolPref = protocol.strValue
  }

  fun getVpnProtocol(): Protocol = Protocol.fromString(protocolPref)

  fun isOnboardingShown() = onboardingPref

  fun onOnboardingShown() {
    onboardingPref = true
  }
}
