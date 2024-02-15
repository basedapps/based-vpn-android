package co.uk.basedapps.vpn.network

import android.content.SharedPreferences
import co.uk.basedapps.vpn.common.provider.AppDetailsProvider
import co.uk.basedapps.vpn.prefs.delegate
import co.uk.basedapps.vpn.prefs.getValue
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
