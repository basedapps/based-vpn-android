package co.uk.basedapps.vpn.di

import android.content.Context
import co.uk.basedapps.domain_wireguard.core.repo.WireguardRepositoryImpl
import co.uk.basedapps.domain_wireguard.core.init.WireguardInitializer
import co.uk.basedapps.domain_wireguard.core.repo.WireguardRepository
import co.uk.basedapps.domain_wireguard.core.store.ConfigStore
import co.uk.basedapps.domain_wireguard.core.store.FileConfigStore
import co.uk.basedapps.domain_wireguard.core.store.TunnelCacheStore
import co.uk.basedapps.domain_wireguard.core.store.TunnelCacheStoreImpl
import co.uk.basedapps.domain_wireguard.core.store.WireguardUserPreferenceStore
import co.uk.basedapps.domain_wireguard.core.store.WireguardUserPreferenceStoreImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class WireguardModule {

  @Provides
  @Singleton
  fun provideConfigStore(
    @ApplicationContext context: Context,
  ): ConfigStore = FileConfigStore(context)

  @Provides
  @Singleton
  fun provideTunnelCacheStore(): TunnelCacheStore = TunnelCacheStoreImpl()

  @Provides
  @Singleton
  fun provideWireguardUserPreferenceStore(
    @ApplicationContext context: Context,
  ): WireguardUserPreferenceStore = WireguardUserPreferenceStoreImpl(context)

  @Provides
  @Singleton
  fun provideWireguardRepository(
    @ApplicationContext context: Context,
    configStore: ConfigStore,
    tunnelCacheStore: TunnelCacheStore,
    userPreferenceStore: WireguardUserPreferenceStore,
  ): WireguardRepository = WireguardRepositoryImpl(context, configStore, tunnelCacheStore, userPreferenceStore)

  @Provides
  @Singleton
  fun provideWireguardInitializer(
      repository: WireguardRepository,
  ) = WireguardInitializer(repository)
}
