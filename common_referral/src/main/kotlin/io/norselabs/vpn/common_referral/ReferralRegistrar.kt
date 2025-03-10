package io.norselabs.vpn.common_referral

interface ReferralRegistrar {

  suspend fun register(userId: String, referrerId: String): Result

  enum class Result {
    Success,
    Fail,
    AlreadyRegistered,
  }
}
