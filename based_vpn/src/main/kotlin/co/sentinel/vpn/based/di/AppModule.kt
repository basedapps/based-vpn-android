package co.sentinel.vpn.based.di

import android.content.SharedPreferences
import co.sentinel.vpn.based.app_config.AppConfig
import co.sentinel.vpn.based.core_impl.user.UserInitializerInteractorImpl
import co.sentinel.vpn.based.core_impl.vpn.VPNConnectorInteractorImpl
import co.sentinel.vpn.based.network.repository.AppRepository
import co.sentinel.vpn.based.storage.AppStorage
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.norselabs.vpn.core_vpn.user.UserInitializer
import io.norselabs.vpn.core_vpn.user.UserInitializerInteractor
import io.norselabs.vpn.core_vpn.vpn.connector.VPNConnector
import io.norselabs.vpn.core_vpn.vpn.connector.VPNConnectorInteractor
import io.norselabs.vpn.core_vpn.vpn.destination.DestinationStorage
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
  fun provideUserInitializerInteractor(
    repository: AppRepository,
    storage: AppStorage,
    config: AppConfig,
  ): UserInitializerInteractor {
    return UserInitializerInteractorImpl(repository, storage, config)
  }

  @Provides
  @Singleton
  fun provideUserInitializer(
    interactor: UserInitializerInteractor,
  ): UserInitializer = UserInitializer(
    scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    interactor = interactor,
  )

  @Provides
  @Singleton
  fun provideVPNConnectorInteractor(
    repository: AppRepository,
    v2RayRepository: V2RayRepository,
    storage: AppStorage,
  ): VPNConnectorInteractor {
    return VPNConnectorInteractorImpl(
      repository = repository,
      v2RayRepository = v2RayRepository,
      storage = storage,
    )
  }

  @Provides
  @Singleton
  fun provideVPNConnector(
    gson: Gson,
    interactor: VPNConnectorInteractor,
  ): VPNConnector {
    return VPNConnector(gson, interactor)
  }

  @Provides
  @Singleton
  fun provideDestinationKeeper(
    gson: Gson,
    prefs: SharedPreferences,
  ): DestinationStorage {
    return DestinationStorage(gson, prefs)
  }
}
