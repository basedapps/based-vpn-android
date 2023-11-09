package co.uk.basedapps.vpn.common.provider

interface AppDetailsProvider {
  fun getAppVersion(): String
  fun getPackage(): String
  fun getBaseUrl(): String
}
