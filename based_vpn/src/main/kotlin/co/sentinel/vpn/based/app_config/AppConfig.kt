package co.sentinel.vpn.based.app_config

interface AppConfig {
  fun getAppVersion(): String
  fun getPackage(): String
  fun getBaseUrl(): String
  fun getBasedAppVersion(): Long
  fun getBasedApiVersion(): Long
  fun getAppToken(): String
}
