package co.sentinel.vpn.based.network

import android.content.SharedPreferences
import co.sentinel.vpn.based.common.provider.AppDetailsProvider
import co.sentinel.vpn.based.prefs.delegate
import co.sentinel.vpn.based.prefs.getValue
import okhttp3.Interceptor
import okhttp3.Response

class HeadersInterceptor(
  private val provider: AppDetailsProvider,
  prefs: SharedPreferences,
) : Interceptor {

  private val token: String by prefs.delegate("device_token", "")

  override fun intercept(chain: Interceptor.Chain): Response = chain.run {
    proceed(
      request()
        .newBuilder()
        .apply {
          addHeader("X-App-Token", provider.getAppToken())
          if (token.isNotEmpty()) {
            addHeader("X-Device-Token", token)
          }
        }
        .build(),
    )
  }
}
