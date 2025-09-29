package io.norselabs.vpn.based.app_config

interface AppConfig {
  fun getAppId(): String
  fun getAppVersion(): String
  fun getPackage(): String
  fun getBaseUrl(): String
  fun getDnsDomain(): String
  fun getProxy(): String?
  fun getAppToken(): String
}
