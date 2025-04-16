package io.norselabs.vpn.common_referral

import android.content.SharedPreferences

class ReferralManager(
  private val prefs: SharedPreferences,
  private val registrar: ReferralRegistrar,
) {

  private var referredIdPref: String?
    get() = prefs.getString("referrer_id", null)
    set(referredId) {
      prefs.edit().putString("referrer_id", referredId).apply()
    }

  suspend fun init(
    tokenProvider: suspend () -> String,
  ) {
    val referredId = referredIdPref.orEmpty()
    if (referredId.isBlank()) return

    val token = tokenProvider()
    if (token.isNotBlank() && referredId.isNotBlank()) {
      val result = registrar.register(token, referredId)
      if (result != ReferralRegistrar.Result.Fail) {
        referredIdPref = ""
      }
    }
  }

  suspend fun registerReferral(
    referrerId: String,
    tokenProvider: suspend () -> String,
  ) {
    referredIdPref = referrerId
    val token = tokenProvider()
    if (token.isNotBlank()) {
      val result = registrar.register(token, referrerId)
      if (result != ReferralRegistrar.Result.Fail) {
        referredIdPref = ""
      }
    }
  }
}
