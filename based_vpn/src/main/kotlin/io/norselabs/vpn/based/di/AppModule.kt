package io.norselabs.vpn.based.di

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import dagger.BindsOptionalOf
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.norselabs.vpn.based.app_config.AppConfig
import io.norselabs.vpn.based.core_impl.vpn.VPNDriverImpl
import io.norselabs.vpn.common.status_card.StatusCardController
import io.norselabs.vpn.common_logger.logger.FileLogTree
import io.norselabs.vpn.common_logger.share.LogsSender
import io.norselabs.vpn.core_vpn.connectivity.NetworkStateMonitor
import io.norselabs.vpn.core_vpn.storage.CoreStorage
import io.norselabs.vpn.core_vpn.user.UserInitializer
import io.norselabs.vpn.core_vpn.vpn.connector.ConnectionLifecycleListener
import io.norselabs.vpn.core_vpn.vpn.connector.VPNConnector
import io.norselabs.vpn.core_vpn.vpn.destination.DestinationStorage
import io.norselabs.vpn.core_vpn.vpn.split_tunneling.SplitTunnelingConfigurator
import io.norselabs.vpn.sdk.dvpn_client.DVPNClient
import io.norselabs.vpn.v2ray.repo.V2RayRepository
import java.util.Optional
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

  @Provides
  @Singleton
  fun provideStatusCardController(): StatusCardController = StatusCardController()

  @Provides
  @Singleton
  fun provideUserInitializer(
    config: AppConfig,
    dvpnClient: DVPNClient,
    coreStorage: CoreStorage,
    networkMonitor: NetworkStateMonitor,
  ): UserInitializer = UserInitializer(
    scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    coreStorage = coreStorage,
    dvpn = dvpnClient,
    networkMonitor = networkMonitor,
    appVersion = config.getAppVersion(),
  )

  @Provides
  @Singleton
  fun provideVPNDriver(
    dvpnClient: DVPNClient,
    v2RayRepository: V2RayRepository,
  ): VPNDriverImpl {
    return VPNDriverImpl(
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
    driver: VPNDriverImpl,
    listener: Optional<ConnectionLifecycleListener>,
  ): VPNConnector {
    return VPNConnector(
      gson = gson,
      dvpn = dvpnClient,
      coreStorage = coreStorage,
      driver = driver,
      listener = listener.orElseGet { object : ConnectionLifecycleListener {} },
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

/**
 * Optional extension points a wrapper app may bind, in addition to the
 * required [AppConfig]. Without an app-side binding the connector runs
 * with a no-op [ConnectionLifecycleListener].
 */
@Module
@InstallIn(SingletonComponent::class)
interface AppCallbacksModule {

  @BindsOptionalOf
  fun connectionLifecycleListener(): ConnectionLifecycleListener
}
