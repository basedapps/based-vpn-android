package io.norselabs.vpn.based.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.norselabs.vpn.based.app_config.AppConfig
import io.norselabs.vpn.based.core_impl.vpn.V2RayTunnelStateProvider
import io.norselabs.vpn.core_vpn.storage.CoreStorage
import io.norselabs.vpn.sdk.common.device_token.DeviceTokenStorage
import io.norselabs.vpn.sdk.common.logger.DvpnLogger
import io.norselabs.vpn.sdk.dvpn_client.DVPNClient
import io.norselabs.vpn.sdk.dvpn_client.DvpnLogLevel
import io.norselabs.vpn.v2ray.repo.V2RayRepository
import javax.inject.Singleton
import timber.log.Timber

@Module
@InstallIn(SingletonComponent::class)
class DvpnModule {

  @Provides
  @Singleton
  fun provideDeviceTokenStorage(
    storage: CoreStorage,
  ): DeviceTokenStorage = object : DeviceTokenStorage {
    override fun saveToken(token: String) {
      storage.setToken(token)
    }

    override fun getToken(): String? {
      return storage.getToken().takeIf { it.isNotBlank() }
    }

    override fun clearToken() {
      storage.setToken("")
    }
  }

  @Provides
  @Singleton
  fun provideDVPN(
    config: AppConfig,
    tokenStorage: DeviceTokenStorage,
    v2RayRepository: V2RayRepository,
    @ApplicationContext context: Context,
  ): DVPNClient {
    return DVPNClient(
      context = context,
      appToken = config.getAppToken(),
      tokenStorage = tokenStorage,
      defaultUrl = config.getBaseUrl(),
      configUrls = listOf(
        "https://api.dvpnsdk.com/mirrors",
        "https://storage.yandexcloud.net/dvpn-sdk/mirrors.json",
        "https://dvpn-sdk-1327780557.cos.ap-guangzhou.myqcloud.com/mirrors.json",
        "https://storage.googleapis.com/dvpn-sdk/mirrors.json",
        "https://dvpn-sdk.s3.us-east-2.amazonaws.com/mirrors.json",
      ),
      logger = object : DvpnLogger {
        override fun log(tag: String, message: String) {
          Timber.tag(tag).d(message)
        }
      },
      logLevel = DvpnLogLevel.BODY,
      tunnelStateProvider = V2RayTunnelStateProvider(v2RayRepository),
    )
  }
}
