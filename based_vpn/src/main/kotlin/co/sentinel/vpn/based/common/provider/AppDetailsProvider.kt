package co.sentinel.vpn.based.common.provider

interface AppDetailsProvider {
  fun getAppVersion(): String
  fun getPackage(): String
  fun getBaseUrl(): String
  fun getBasedAppVersion(): Long
  fun getBasedApiVersion(): Long
  fun getAppToken(): String
}
