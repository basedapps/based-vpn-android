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
    userIdProvider: suspend () -> String,
  ) {
    val referredId = referredIdPref.orEmpty()
    if (referredId.isBlank()) return

    val userId = userIdProvider()
    if (userId.isNotBlank() && referredId.isNotBlank()) {
      val result = registrar.register(userId, referredId)
      if (result != ReferralRegistrar.Result.Fail) {
        referredIdPref = ""
      }
    }
  }

  suspend fun registerReferral(
    referrerId: String,
    userIdProvider: suspend () -> String,
  ) {
    referredIdPref = referrerId
    val userId = userIdProvider()
    if (userId.isNotBlank()) {
      val result = registrar.register(userId, referrerId)
      if (result != ReferralRegistrar.Result.Fail) {
        referredIdPref = ""
      }
    }
  }
}
