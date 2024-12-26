package io.norselabs.vpn.common_network

import okhttp3.Interceptor
import okhttp3.Response

class HeadersInterceptor(
  private val appToken: String,
  private val userTokenProvider: () -> String,
) : Interceptor {

  override fun intercept(chain: Interceptor.Chain): Response = chain.run {
    val token = userTokenProvider()
    proceed(
      request()
        .newBuilder()
        .apply {
          addHeader("X-App-Token", appToken)
          if (token.isNotEmpty()) {
            addHeader("X-Device-Token", token)
          }
        }
        .build(),
    )
  }
}
