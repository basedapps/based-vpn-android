package co.sentinel.vpn.based.di

import android.content.Context
import co.sentinel.vpn.wireguard.control.WireguardInitializer
import co.sentinel.vpn.wireguard.repo.WireguardRepository
import co.sentinel.vpn.wireguard.repo.WireguardRepositoryImpl
import co.sentinel.vpn.wireguard.store.ConfigStore
import co.sentinel.vpn.wireguard.store.FileConfigStore
import co.sentinel.vpn.wireguard.store.TunnelCacheStore
import co.sentinel.vpn.wireguard.store.TunnelCacheStoreImpl
import co.sentinel.vpn.wireguard.store.WireguardUserPreferenceStore
import co.sentinel.vpn.wireguard.store.WireguardUserPreferenceStoreImpl
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
  fun provideConfigStore(@ApplicationContext context: Context): ConfigStore = FileConfigStore(context)

  @Provides
  @Singleton
  fun provideTunnelCacheStore(): TunnelCacheStore = TunnelCacheStoreImpl()

  @Provides
  @Singleton
  fun provideWireguardUserPreferenceStore(@ApplicationContext context: Context): WireguardUserPreferenceStore =
    WireguardUserPreferenceStoreImpl(context)

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
  fun provideWireguardInitializer(repository: WireguardRepository) = WireguardInitializer(repository)
}
