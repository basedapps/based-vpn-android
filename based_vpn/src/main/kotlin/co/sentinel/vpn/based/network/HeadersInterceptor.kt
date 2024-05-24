package co.sentinel.vpn.based.network

import android.content.SharedPreferences
import co.sentinel.vpn.based.app_config.AppConfig
import co.sentinel.vpn.based.shared_preferences.delegate
import co.sentinel.vpn.based.shared_preferences.getValue
import okhttp3.Interceptor
import okhttp3.Response

class HeadersInterceptor(
  private val config: AppConfig,
  prefs: SharedPreferences,
) : Interceptor {

  private val token: String by prefs.delegate("device_token", "")

  override fun intercept(chain: Interceptor.Chain): Response = chain.run {
    proceed(
      request()
        .newBuilder()
        .apply {
          addHeader("X-App-Token", config.getAppToken())
          if (token.isNotEmpty()) {
            addHeader("X-Device-Token", token)
          }
        }
        .build(),
    )
  }
}
