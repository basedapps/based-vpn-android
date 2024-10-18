package co.sentinel.vpn.based.di

import co.sentinel.vpn.based.app_config.AppConfig
import co.sentinel.vpn.based.core.user.UserInitializer
import co.sentinel.vpn.based.core.user.UserInitializerInteractor
import co.sentinel.vpn.based.core_impl.user.UserInitializerInteractorImpl
import co.sentinel.vpn.based.network.repository.BasedRepository
import co.sentinel.vpn.based.storage.BasedStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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
    repository: BasedRepository,
    storage: BasedStorage,
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
}
