package co.sentinel.vpn.based.storage

import android.content.SharedPreferences
import io.norselabs.vpn.common.preferences.delegate
import io.norselabs.vpn.common.preferences.getValue
import io.norselabs.vpn.common.preferences.setValue
import io.norselabs.vpn.core_vpn.vpn.Protocol
import javax.inject.Inject

class AppStorage
@Inject constructor(prefs: SharedPreferences) {

  private var tokenPref: String by prefs.delegate("device_token", "")

  private var userIdPref: String by prefs.delegate("user_id", "")

  private var protocolPref by prefs.delegate("selected_protocol", "")

  private var onboardingPref by prefs.delegate("onboarding_shown", false)

  private var ratingPref by prefs.delegate("rating_status", RatingStatus.New.name)

  fun storeToken(token: String) {
    tokenPref = token
  }

  fun getToken(): String = tokenPref

  fun setUserId(userId: String) {
    userIdPref = userId
  }

  fun getUserId(): String = userIdPref

  fun clearUserData() {
    tokenPref = ""
    userIdPref = ""
  }

  fun storeVpnProtocol(protocol: Protocol) {
    protocolPref = protocol.strValue
  }

  fun getVpnProtocol(): Protocol = Protocol.fromString(protocolPref)

  fun isOnboardingShown() = onboardingPref

  fun onOnboardingShown() {
    onboardingPref = true
  }

  fun setRatingStatus(status: RatingStatus) {
    ratingPref = status.name
  }

  fun getRatingStatus(): RatingStatus {
    return try {
      RatingStatus.valueOf(ratingPref)
    } catch (e: IllegalArgumentException) {
      RatingStatus.New
    }
  }
}
