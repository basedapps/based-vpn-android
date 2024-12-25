package io.norselabs.vpn.based.network

import android.content.SharedPreferences
import io.norselabs.vpn.based.app_config.AppConfig
import io.norselabs.vpn.common.preferences.delegate
import io.norselabs.vpn.common.preferences.getValue
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
