package io.norselabs.vpn.common_referral

interface ReferralRegistrar {

  suspend fun register(token: String, referrerId: String): Result

  enum class Result {
    Success,
    Fail,
    AlreadyRegistered,
  }
}
