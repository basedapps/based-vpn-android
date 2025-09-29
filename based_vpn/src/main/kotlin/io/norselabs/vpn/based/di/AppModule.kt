package io.norselabs.vpn.based.di

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.norselabs.vpn.based.app_config.AppConfig
import io.norselabs.vpn.based.core_impl.vpn.VPNInteractorImpl
import io.norselabs.vpn.common_logger.logger.FileLogTree
import io.norselabs.vpn.common_logger.share.LogsSender
import io.norselabs.vpn.core_vpn.connectivity.NetworkStateMonitor
import io.norselabs.vpn.core_vpn.storage.CoreStorage
import io.norselabs.vpn.core_vpn.user.UserInitializer
import io.norselabs.vpn.core_vpn.vpn.connector.VPNConnector
import io.norselabs.vpn.core_vpn.vpn.connector.VPNInteractor
import io.norselabs.vpn.core_vpn.vpn.destination.DestinationStorage
import io.norselabs.vpn.core_vpn.vpn.split_tunneling.SplitTunnelingConfigurator
import io.norselabs.vpn.sdk.dvpn_client.DVPNClient
import io.norselabs.vpn.v2ray.repo.V2RayRepository
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

  @Provides
  @Singleton
  fun provideUserInitializer(
    config: AppConfig,
    dvpnClient: DVPNClient,
    coreStorage: CoreStorage,
  ): UserInitializer = UserInitializer(
    scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    coreStorage = coreStorage,
    dvpn = dvpnClient,
    appVersion = config.getAppVersion(),
  )

  @Provides
  @Singleton
  fun provideVPNConnectorInteractor(
    dvpnClient: DVPNClient,
    v2RayRepository: V2RayRepository,
  ): VPNInteractor {
    return VPNInteractorImpl(
      dvpnClient = dvpnClient,
      v2RayRepository = v2RayRepository,
    )
  }

  @Provides
  @Singleton
  fun provideVPNConnector(
    gson: Gson,
    coreStorage: CoreStorage,
    dvpnClient: DVPNClient,
    interactor: VPNInteractor,
  ): VPNConnector {
    return VPNConnector(
      gson = gson,
      dvpn = dvpnClient,
      coreStorage = coreStorage,
      interactor = interactor,
    )
  }

  @Provides
  @Singleton
  fun provideDestinationKeeper(
    gson: Gson,
    prefs: SharedPreferences,
  ): DestinationStorage {
    return DestinationStorage(gson, prefs)
  }

  @Provides
  @Singleton
  fun provideSplitTunnelingConfigurator(
    v2RayRepository: V2RayRepository,
  ): SplitTunnelingConfigurator {
    return SplitTunnelingConfigurator(v2RayRepository)
  }

  @Provides
  @Singleton
  fun provideNetworkStateMonitor(
    @ApplicationContext context: Context,
  ): NetworkStateMonitor {
    return NetworkStateMonitor(context)
  }

  @Provides
  @Singleton
  fun provideLogsSender(
    config: AppConfig,
    fileLogTree: FileLogTree,
  ): LogsSender {
    return LogsSender(appId = config.getAppId(), fileLogTree = fileLogTree)
  }
}
