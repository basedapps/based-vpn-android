package io.norselabs.vpn.based.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.norselabs.vpn.core_vpn.vpn.dns.DnsConfigurator
import io.norselabs.vpn.v2ray.repo.V2RayRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class V2RayModule {

  @Provides
  @Singleton
  fun provideV2RayRepository(
    @ApplicationContext context: Context,
  ): V2RayRepository = V2RayRepository(context)

  @Provides
  @Singleton
  fun provideDnsConfigurator(
    v2Ray: V2RayRepository,
  ): DnsConfigurator {
    return DnsConfigurator(v2Ray)
  }
}
