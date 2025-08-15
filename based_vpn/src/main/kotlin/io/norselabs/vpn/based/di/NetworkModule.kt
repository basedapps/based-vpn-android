package io.norselabs.vpn.based.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.norselabs.vpn.based.app_config.AppConfig
import io.norselabs.vpn.core_vpn.storage.CoreStorage
import io.norselabs.vpn.sdk.common.config.DVPNConfigStorage
import io.norselabs.vpn.sdk.common.device_token.DeviceTokenStorage
import io.norselabs.vpn.sdk.common.logger.DvpnLogger
import io.norselabs.vpn.sdk.dvpn_client.DVPNClient
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
  fun provideDVPNConfigStorage(
    storage: CoreStorage,
  ): DVPNConfigStorage {
    return object : DVPNConfigStorage {
      override fun isAlternativeRouting(): Boolean {
        return storage.isAlternativeRouting()
      }

      override fun setAlternativeRouting(isEnabled: Boolean) {
        storage.setAlternativeRouting(isEnabled)
      }
    }
  }

  @Provides
  @Singleton
  fun provideDVPN(
    config: AppConfig,
    tokenStorage: DeviceTokenStorage,
    configStorage: DVPNConfigStorage,
  ): DVPNClient {
    return DVPNClient(
      appToken = config.getAppToken(),
      tokenStorage = tokenStorage,
      configStorage = configStorage,
      baseRestUrl = config.getBaseUrl(),
      dnsDomain = config.getDnsDomain(),
      logger = object : DvpnLogger {
        override fun log(tag: String, message: String) {
          Timber.tag(tag).d(message)
        }
      },
    )
  }
}
